package org.reconan;

import org.reconan.model.Entity;
import org.reconan.model.EntityType;
import org.reconan.service.InvestigationService;

import java.util.List;

public class OsintTest {

    private static final String SEPARATEUR =
        "============================================================";
    private static final String SEPARATEUR_LEGER =
        "------------------------------------------------------------";

    public static void main(String[] args) {

        InvestigationService service = new InvestigationService();

        System.out.println(SEPARATEUR);
        System.out.println("  ReConan - Test du Moteur OSINT");
        System.out.println(SEPARATEUR);
        System.out.println();

        // TEST 1 : Adresse IP
        System.out.println("TEST 1 : Enrichissement d'une adresse IP");
        System.out.println(SEPARATEUR_LEGER);

        Entity ip = new Entity(EntityType.IP, "8.8.8.8");
        System.out.println("Entite cible : IP -> " + ip.getValue());
        System.out.println();

        List<Entity> resultatsIP = service.enrich(ip);

        System.out.println("Metadonnees collectees sur l'IP :");
        ip.getMetadata().forEach((cle, val) ->
            System.out.println("  " + cle + " : " + val));

        System.out.println();
        System.out.println("Nouvelles entites decouvertes (" + resultatsIP.size() + ") :");
        for (Entity e : resultatsIP) {
            System.out.println("  [" + e.getType() + "] " + e.getValue());
            e.getMetadata().forEach((cle, val) ->
                System.out.println("    " + cle + " : " + val));
        }
        System.out.println();

        // TEST 2 : Nom de domaine
        System.out.println("TEST 2 : Enrichissement d'un domaine");
        System.out.println(SEPARATEUR_LEGER);

        Entity domaine = new Entity(EntityType.DOMAIN, "github.com");
        System.out.println("Entite cible : DOMAIN -> " + domaine.getValue());
        System.out.println();

        List<Entity> resultatsDomaine = service.enrich(domaine);

        System.out.println("Metadonnees collectees sur le domaine :");
        domaine.getMetadata().forEach((cle, val) ->
            System.out.println("  " + cle + " : " + val));

        System.out.println();
        System.out.println("Nouvelles entites decouvertes (" + resultatsDomaine.size() + ") :");
        for (Entity e : resultatsDomaine) {
            System.out.println("  [" + e.getType() + "] " + e.getValue());
            e.getMetadata().forEach((cle, val) ->
                System.out.println("    " + cle + " : " + val));
        }
        System.out.println();

        // TEST 3 : Adresse email
        System.out.println("TEST 3 : Enrichissement d'une adresse email");
        System.out.println(SEPARATEUR_LEGER);

        Entity email = new Entity(EntityType.EMAIL, "test@gmail.com");
        System.out.println("Entite cible : EMAIL -> " + email.getValue());
        System.out.println();

        List<Entity> resultatsEmail = service.enrich(email);

        System.out.println("Metadonnees collectees sur l'email :");
        email.getMetadata().forEach((cle, val) ->
            System.out.println("  " + cle + " : " + val));

        System.out.println();
        System.out.println("Nouvelles entites decouvertes (" + resultatsEmail.size() + ") :");
        for (Entity e : resultatsEmail) {
            System.out.println("  [" + e.getType() + "] " + e.getValue());
            e.getMetadata().forEach((cle, val) ->
                System.out.println("    " + cle + " : " + val));
        }
        System.out.println();

        // TEST 4 : Nom d'utilisateur
        System.out.println("TEST 4 : Enrichissement d'un nom d'utilisateur");
        System.out.println(SEPARATEUR_LEGER);

        Entity utilisateur = new Entity(EntityType.USERNAME, "torvalds");
        System.out.println("Entite cible : USERNAME -> " + utilisateur.getValue());
        System.out.println();

        List<Entity> resultatsUtilisateur = service.enrich(utilisateur);

        System.out.println("Metadonnees collectees sur l'utilisateur :");
        utilisateur.getMetadata().forEach((cle, val) ->
            System.out.println("  " + cle + " : " + val));

        System.out.println();
        System.out.println("Nouvelles entites decouvertes (" + resultatsUtilisateur.size() + ") :");
        for (Entity e : resultatsUtilisateur) {
            System.out.println("  [" + e.getType() + "] " + e.getValue());
            e.getMetadata().forEach((cle, val) ->
                System.out.println("    " + cle + " : " + val));
        }
        System.out.println();

        // RESUME
        System.out.println(SEPARATEUR);
        System.out.println("  RESUME DES TESTS");
        System.out.println(SEPARATEUR);
        System.out.printf("  IP       (8.8.8.8)       : %d entite(s) decouverte(s)%n",
            resultatsIP.size());
        System.out.printf("  DOMAIN   (github.com)    : %d entite(s) decouverte(s)%n",
            resultatsDomaine.size());
        System.out.printf("  EMAIL    (test@gmail.com): %d entite(s) decouverte(s)%n",
            resultatsEmail.size());
        System.out.printf("  USERNAME (torvalds)      : %d entite(s) decouverte(s)%n",
            resultatsUtilisateur.size());
        System.out.println(SEPARATEUR);
        System.out.println();
        System.out.println("Note : Les transforms necessitant une cle API");
        System.out.println("retournent 0 resultat si la cle est absente du .env");
        System.out.println("IPInfo et GitHub fonctionnent sans cle (mode gratuit).");
        System.out.println();
    }
}
