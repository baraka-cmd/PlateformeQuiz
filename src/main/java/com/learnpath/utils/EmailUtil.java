package com.learnpath.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * EmailUtil - Utilitaire d'envoi d'emails via Gmail SMTP
 * 
 * Permet d'envoyer des emails transactionnels :
 * - Réinitialisation de mot de passe
 * - Notifications de bienvenue
 * - Alertes et notifications diverses
 * 
 * @author LearnPath
 * @version 1.0
 */
public class EmailUtil {

    // Configuration Gmail SMTP
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;
    private static final String USERNAME = "barakantwali06@gmail.com";
    private static final String PASSWORD = "gqjv avqz rooo iiio"; // Mot de passe d'application (sans espaces)
    
    // Nom de l'expéditeur
    private static final String FROM_NAME = "LearnPath"; 
    private static final String FROM_EMAIL = "barakantwali06@gmail.com";

    /**
     * Envoie un email de réinitialisation de mot de passe
     * 
     * @param destinataireEmail L'adresse email du destinataire
     * @param destinataireNom   Le nom du destinataire
     * @param nouveauMotDePasse Le nouveau mot de passe temporaire
     * @return true si l'envoi a réussi, false sinon
     */
    public static boolean envoyerMotDePasseOublie(String destinataireEmail, String destinataireNom, String nouveauMotDePasse) {
        String sujet = "🔑 LearnPath - Réinitialisation de votre mot de passe";
        
        String contenu = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<style>"
                + "body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f6fa; margin: 0; padding: 0; }"
                + ".container { max-width: 550px; margin: 30px auto; background: white; border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,0.08); overflow: hidden; }"
                + ".header { background: linear-gradient(135deg, #667eea, #764ba2); padding: 30px; text-align: center; }"
                + ".header h1 { color: white; font-size: 24px; margin: 0; }"
                + ".header i { font-size: 48px; color: white; margin-bottom: 10px; }"
                + ".body { padding: 30px; color: #444; line-height: 1.7; }"
                + ".password-box { background: #f0f4ff; border: 2px dashed #667eea; border-radius: 12px; padding: 20px; text-align: center; margin: 20px 0; }"
                + ".password { font-size: 28px; font-weight: 700; color: #4a00e0; letter-spacing: 2px; font-family: 'Courier New', monospace; }"
                + ".btn { display: inline-block; background: linear-gradient(135deg, #667eea, #764ba2); color: white; text-decoration: none; padding: 14px 30px; border-radius: 10px; font-weight: 600; margin-top: 15px; }"
                + ".footer { background: #fafafa; padding: 20px; text-align: center; color: #999; font-size: 12px; border-top: 1px solid #eee; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<div class=\"header\">"
                + "<p style=\"font-size:48px;margin:0;\">🧠</p>"
                + "<h1>LearnPath</h1>"
                + "<p style=\"color:rgba(255,255,255,0.9);margin:8px 0 0;\">Plateforme de Quiz Interactive</p>"
                + "</div>"
                + "<div class=\"body\">"
                + "<p>Bonjour <strong>" + destinataireNom + "</strong>,</p>"
                + "<p>Vous avez demandé la réinitialisation de votre mot de passe. Voici votre <strong>nouveau mot de passe temporaire</strong> :</p>"
                + "<div class=\"password-box\">"
                + "<p style=\"font-size:13px;color:#888;margin:0 0 8px;\">Votre nouveau mot de passe</p>"
                + "<p class=\"password\">" + nouveauMotDePasse + "</p>"
                + "</div>"
                + "<p style=\"text-align:center;\">"
                + "<a href=\"#\" class=\"btn\">🔐 Se connecter avec ce mot de passe</a>"
                + "</p>"
                + "<p style=\"font-size:13px;color:#999;margin-top:20px;\">"
                + "⚠️ Pour des raisons de sécurité, nous vous recommandons de <strong>changer ce mot de passe</strong> après vous être connecté.<br><br>"
                + "Si vous n'avez pas demandé cette réinitialisation, ignorez simplement cet email. Votre mot de passe actuel reste inchangé."
                + "</p>"
                + "</div>"
                + "<div class=\"footer\">"
                + "<p>© " + java.time.Year.now().getValue() + " LearnPath. Tous droits réservés.</p>"
                + "<p>Développé avec ❤️ par l'équipe LearnPath</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        return envoyerEmail(destinataireEmail, sujet, contenu);
    }

    /**
     * Envoie un email de bienvenue après inscription
     * 
     * @param destinataireEmail L'adresse email du destinataire
     * @param destinataireNom   Le pseudo du joueur
     * @return true si l'envoi a réussi, false sinon
     */
    public static boolean envoyerBienvenue(String destinataireEmail, String destinataireNom) {
        String sujet = "🎉 Bienvenue sur LearnPath, " + destinataireNom + " !";
        
        String contenu = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family:'Segoe UI',Arial,sans-serif;background:#f5f6fa;margin:0;padding:0;\">"
                + "<div style=\"max-width:550px;margin:30px auto;background:white;border-radius:16px;box-shadow:0 8px 30px rgba(0,0,0,0.08);overflow:hidden;\">"
                + "<div style=\"background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;text-align:center;\">"
                + "<p style=\"font-size:48px;margin:0;\">🧠</p>"
                + "<h1 style=\"color:white;font-size:24px;margin:10px 0 0;\">Bienvenue sur LearnPath !</h1>"
                + "</div>"
                + "<div style=\"padding:30px;color:#444;line-height:1.7;\">"
                + "<p>Bonjour <strong>" + destinataireNom + "</strong>,</p>"
                + "<p>Merci d'avoir créé votre compte sur <strong>LearnPath</strong> ! 🚀</p>"
                + "<p>Vous pouvez maintenant :</p>"
                + "<ul style=\"padding-left:20px;\">"
                + "<li>🎯 Explorer les parcours thématiques</li>"
                + "<li>📝 Passer des quiz chronométrés</li>"
                + "<li>🏆 Débloquer des niveaux et suivre votre progression</li>"
                + "</ul>"
                + "<p style=\"text-align:center;margin-top:25px;\">"
                + "<a href=\"#\" style=\"display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);color:white;text-decoration:none;padding:14px 30px;border-radius:10px;font-weight:600;\">"
                + "▶️ Commencer mon parcours</a></p>"
                + "</div>"
                + "<div style=\"background:#fafafa;padding:20px;text-align:center;color:#999;font-size:12px;border-top:1px solid #eee;\">"
                + "<p>© " + java.time.Year.now().getValue() + " LearnPath - Développé avec ❤️</p>"
                + "</div></div></body></html>";

        return envoyerEmail(destinataireEmail, sujet, contenu);
    }

    /**
     * Envoie une notification de déblocage de niveau
     * 
     * @param destinataireEmail L'adresse email du destinataire
     * @param destinataireNom   Le pseudo du joueur
     * @param niveauSuperieur   Le niveau débloqué (ex: "Moyen")
     * @param categorie         La catégorie concernée
     * @return true si l'envoi a réussi, false sinon
     */
    public static boolean envoyerNotificationNiveauDebloque(String destinataireEmail, String destinataireNom, String niveauSuperieur, String categorie) {
        String sujet = "🏆 Niveau " + niveauSuperieur + " débloqué - " + categorie;
        
        String contenu = "<!DOCTYPE html>"
                + "<html><head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family:'Segoe UI',Arial,sans-serif;background:#f5f6fa;margin:0;padding:0;\">"
                + "<div style=\"max-width:550px;margin:30px auto;background:white;border-radius:16px;box-shadow:0 8px 30px rgba(0,0,0,0.08);overflow:hidden;\">"
                + "<div style=\"background:linear-gradient(135deg,#f39c12,#e67e22);padding:30px;text-align:center;\">"
                + "<p style=\"font-size:48px;margin:0;\">🏆</p>"
                + "<h1 style=\"color:white;font-size:24px;margin:10px 0 0;\">Niveau débloqué !</h1>"
                + "</div>"
                + "<div style=\"padding:30px;color:#444;line-height:1.7;\">"
                + "<p>Félicitations <strong>" + destinataireNom + "</strong> ! 🎉</p>"
                + "<p>Vous avez débloqué le niveau <strong style=\"color:#e67e22;\">" + niveauSuperieur + "</strong> dans la catégorie <strong>" + categorie + "</strong>.</p>"
                + "<p>Continuez votre progression et devenez expert !</p>"
                + "</div>"
                + "<div style=\"background:#fafafa;padding:20px;text-align:center;color:#999;font-size:12px;border-top:1px solid #eee;\">"
                + "<p>© " + java.time.Year.now().getValue() + " LearnPath</p>"
                + "</div></div></body></html>";

        return envoyerEmail(destinataireEmail, sujet, contenu);
    }

    /**
     * Méthode privée générique pour envoyer un email via Gmail SMTP
     * 
     * @param destinataire L'adresse email du destinataire
     * @param sujet        Le sujet de l'email
     * @param contenuHTML  Le contenu HTML de l'email
     * @return true si l'envoi a réussi, false sinon
     */
    private static boolean envoyerEmail(String destinataire, String sujet, String contenuHTML) {
        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", String.valueOf(PORT));
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Création de la session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD.replace(" ", ""));
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setContent(contenuHTML, "text/html; charset=UTF-8");

            // Envoi
            Transport.send(message);
            
            System.out.println("✅ Email envoyé avec succès à : " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email à : " + destinataire);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}