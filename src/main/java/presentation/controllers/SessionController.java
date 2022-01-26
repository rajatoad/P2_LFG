package presentation.controllers;

import io.javalin.Javalin;
import presentation.handlers.GameHandler;
import presentation.handlers.SessionHandler;
import static io.javalin.apibuilder.ApiBuilder.*;


public class SessionController {

    private final SessionHandler sessionHandler = new SessionHandler();
    private final GameHandler gameHandler = new GameHandler();

    public void setEndpoints(Javalin app) {
        app.routes(() -> {

            path("/game", () -> {

              path("/available", () ->{
                  get(gameHandler.getGameSessionsList);
              });

              path("/select", () -> {
                 get(gameHandler.getGameGroupSessions);
              });

            });

            path("/group", () -> {

                path("/host", () -> {
                    post(sessionHandler.createGroupSession);
                });


            });

        });
    }
}