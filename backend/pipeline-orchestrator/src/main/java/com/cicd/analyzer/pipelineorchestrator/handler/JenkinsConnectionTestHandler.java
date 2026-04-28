package com.cicd.analyzer.pipelineorchestrator.handler;

import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import com.cicd.analyzer.pipelineorchestrator.enums.TestStatus;
import com.cicd.analyzer.pipelineorchestrator.exception.JenkinsConnectionException;
import com.cicd.analyzer.pipelineorchestrator.service.JenkinsConnectionService;
import com.cicd.analyzer.pipelineorchestrator.service.JenkinsDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler pour tester une connexion Jenkins
 * Orchestre le test de connectivité et la mise à jour du statut
 */
@Service
@RequiredArgsConstructor
@Transactional
public class JenkinsConnectionTestHandler {

    private final JenkinsConnectionService jenkinsConnectionService;
    private final JenkinsDataService jenkinsDataService;

    /**
     * Tester une connexion Jenkins en appelant l'endpoint /server-info
     *
     * WORKFLOW:
     * 1. Récupérer la connexion depuis la BDD
     * 2. Tenter d'appeler /server-info via jenkins-connector
     * 3. Si succès → Mettre à jour le statut à SUCCESS
     * 4. Si échec → Mettre à jour le statut à FAILED
     * 5. Retourner le TestStatus
     *
     * @param connectionId ID de la connexion à tester
     * @param userEmail Email de l'utilisateur (pour vérification propriété)
     * @return TestStatus (SUCCESS, FAILED)
     */
    public TestStatus testConnection(Long connectionId, String userEmail) {
        JenkinsConnection connection = null;

        try {
            System.out.println("🔍 Test de connexion Jenkins (ID: " + connectionId + ")");

            // 1. Récupérer la connexion (avec vérification propriété user)
            connection = jenkinsConnectionService.findById(connectionId, userEmail);
            System.out.println("  ✓ Connexion récupérée: " + connection.getName());

            // 2. Tenter d'appeler /server-info pour tester la connectivité
            System.out.println("  🔌 Tentative de connexion à " + connection.getUrl() + "...");
            String serverInfo = jenkinsDataService.getServerInfo(connection);

            // Si on arrive ici, la connexion a réussi
            System.out.println("  ✅ Connexion réussie !");

            // 3. Mettre à jour le statut à SUCCESS
            jenkinsConnectionService.updateTestStatus(connectionId, TestStatus.SUCCESS);

            System.out.println("✅ Test de connexion réussi !");
            return TestStatus.SUCCESS;

        } catch (Exception e) {
            System.err.println("  ❌ Échec de connexion: " + e.getMessage());

            // 4. Mettre à jour le statut à FAILED
            if (connection != null) {
                try {
                    jenkinsConnectionService.updateTestStatus(connectionId, TestStatus.FAILED);
                } catch (Exception updateError) {
                    System.err.println("  ⚠️ Impossible de mettre à jour le statut: " + updateError.getMessage());
                }
            }

            System.err.println("❌ Test de connexion échoué !");
            return TestStatus.FAILED;
        }
    }

    /**
     * Tester une connexion Jenkins sans vérification de propriété user
     * (utilisé lors de la création d'une connexion)
     *
     * @param connectionId ID de la connexion à tester
     * @return TestStatus (SUCCESS, FAILED)
     */
    public TestStatus testConnectionWithoutUserCheck(Long connectionId) {
        JenkinsConnection connection = null;

        try {
            System.out.println("🔍 Test de connexion Jenkins (ID: " + connectionId + ") - sans vérification user");

            // Récupérer la connexion sans vérification user
            connection = jenkinsConnectionService.findById(connectionId);
            System.out.println("  ✓ Connexion récupérée: " + connection.getName());

            // Tenter d'appeler /server-info
            System.out.println("  🔌 Tentative de connexion à " + connection.getUrl() + "...");
            String serverInfo = jenkinsDataService.getServerInfo(connection);

            // Connexion réussie
            System.out.println("  ✅ Connexion réussie !");
            jenkinsConnectionService.updateTestStatus(connectionId, TestStatus.SUCCESS);

            System.out.println("✅ Test de connexion réussi !");
            return TestStatus.SUCCESS;

        } catch (Exception e) {
            System.err.println("  ❌ Échec de connexion: " + e.getMessage());

            // Mettre à jour le statut à FAILED
            if (connection != null) {
                try {
                    jenkinsConnectionService.updateTestStatus(connectionId, TestStatus.FAILED);
                } catch (Exception updateError) {
                    System.err.println("  ⚠️ Impossible de mettre à jour le statut: " + updateError.getMessage());
                }
            }

            System.err.println("❌ Test de connexion échoué !");
            return TestStatus.FAILED;
        }
    }

    /**
     * Tester une connexion avec des credentials fournis directement
     * (utilisé avant la création pour valider les credentials)
     * Ne sauvegarde pas dans la BDD
     *
     * @param url URL Jenkins
     * @param username Username Jenkins
     * @param password Password Jenkins
     * @return true si connexion réussie, false sinon
     */
    public boolean testCredentials(String url, String username, String password) {
        try {
            System.out.println("🔍 Test de credentials Jenkins: " + url);

            // Créer une connexion temporaire (non sauvegardée)
            JenkinsConnection tempConnection = new JenkinsConnection();
            tempConnection.setUrl(url);
            tempConnection.setUsername(username);
            tempConnection.setPassword(password);

            // Tenter d'appeler /server-info
            System.out.println("  🔌 Tentative de connexion...");
            String serverInfo = jenkinsDataService.getServerInfo(tempConnection);

            // Si on arrive ici, la connexion a réussi
            System.out.println("  ✅ Credentials valides !");
            return true;

        } catch (Exception e) {
            System.err.println("  ❌ Credentials invalides: " + e.getMessage());
            return false;
        }
    }
}