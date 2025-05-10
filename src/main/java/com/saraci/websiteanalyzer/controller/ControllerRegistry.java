package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.config.AppConfig;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;
import com.saraci.websiteanalyzer.repository.ScheduleRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.service.WebsiteAnalyzerService;
import com.saraci.websiteanalyzer.service.scheduler.SchedulerService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Registriert und verwaltet alle Controller.
 */
public class ControllerRegistry {
    private static final Logger logger = Logger.getLogger(ControllerRegistry.class.getName());

    private final List<Controller> controllers = new ArrayList<>();

    /**
     * Erstellt eine neue Controller-Registry mit allen benötigten Controllern.
     */
    public ControllerRegistry(AppConfig appConfig, SchedulerService schedulerService) {
        // Repositories aus der App-Konfiguration holen
        WebsiteRepository websiteRepository = appConfig.getWebsiteRepository();
        AnalysisResultRepository analysisResultRepository = appConfig.getAnalysisResultRepository();
        ScheduleRepository scheduleRepository = appConfig.getScheduleRepository();

        // Service aus der App-Konfiguration holen
        WebsiteAnalyzerService analyzerService = appConfig.getWebsiteAnalyzerService();

        // Controller erstellen und registrieren
        controllers.add(new AnalysisController(analyzerService, websiteRepository, analysisResultRepository));
        controllers.add(new WebsiteController(websiteRepository, analysisResultRepository));
        controllers.add(new ScheduleController(scheduleRepository, websiteRepository, schedulerService));

        logger.info("Controller-Registry initialisiert mit " + controllers.size() + " Controllern");
    }

    /**
     * Registriert die Routen aller Controller.
     */
    public void registerAllRoutes() {
        for (Controller controller : controllers) {
            controller.registerRoutes();
        }
        logger.info("Alle Controller-Routen registriert");
    }
}