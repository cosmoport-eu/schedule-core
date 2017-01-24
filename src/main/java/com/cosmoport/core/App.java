package com.cosmoport.core;

import com.cosmoport.core.api.ApiV0Module;
import com.cosmoport.core.api.error.ApiExceptionMapper;
import com.cosmoport.core.config.Config;
import com.cosmoport.core.module.JsonModule;
import com.cosmoport.core.module.LoggerModule;
import com.cosmoport.core.scheduler.SchedulerModule;
import com.cosmoport.core.socket.EventServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.*;
import com.google.inject.servlet.ServletModule;
import com.palominolabs.http.server.HttpServerWrapperModule;
import de.skuzzle.inject.async.GuiceAsync;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.interceptors.GZIPEncodingInterceptor;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

public class App {
    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new ServiceModule());

        injector.getAllBindings();

        injector.createChildInjector().getAllBindings();

        Server server = new Server(Config.PORT);
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        servletHandler.addServlet(sh, "/*");


        // Add a websocket to a specific path spec
        ServletHolder holderEvents = new ServletHolder("ws-events", EventServlet.class);
        servletHandler.addServlet(holderEvents, "/events/*");

        server.setHandler(servletHandler);
        server.start();
        server.join();
    }

    private static class ServiceModule extends AbstractModule {
        @Override
        protected void configure() {
            binder().requireExplicitBindings();

            GuiceAsync.enableFor(binder());

            install(new HttpServerWrapperModule());
            install(new JsonModule());
            install(new LoggerModule(App00.class));
            install(new ApiV0Module());
            install(new SchedulerModule());

            bind(GuiceResteasyBootstrapServletContextListener.class);
            bind(ApiExceptionMapper.class);
            bind(GZIPEncodingInterceptor.class);

            install(new ServletModule() {
                @Override
                protected void configureServlets() {
                    bind(HttpServletDispatcher.class).in(Scopes.SINGLETON);
                    serve("/*").with(HttpServletDispatcher.class);
                }
            });
        }

        @Provides
        @Singleton
        JacksonJsonProvider getJacksonJsonProvider(ObjectMapper objectMapper) {
            return new JacksonJsonProvider(objectMapper);
        }
    }
}
