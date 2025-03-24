CREATE TABLE budget(
                       budget_id INT unsigned NOT NULL AUTO_INCREMENT,
                       valeur DECIMAL(10,2) NOT NULL,
                       date_budget datetime NOT NULL,
                       customer_id INT unsigned NOT NULL,
                       PRIMARY KEY(budget_id),
                       FOREIGN KEY(customer_id) REFERENCES customer(customer_id)
);


CREATE TABLE notification(
                             notification_id INT unsigned NOT NULL AUTO_INCREMENT,
                             message VARCHAR(250) NOT NULL,
                             date_notification DATETIME NOT NULL,
                             etat INT DEFAULT NULL,
                             customer_id INT unsigned NOT NULL,
                            id_depense int references depense(depense_id),
                             PRIMARY KEY(notification_id),
                             FOREIGN KEY(customer_id) REFERENCES customer(customer_id)
);
alter table notification add id_depense int references depense(depense_id);

CREATE TABLE depense(
                        depense_id INT unsigned NOT NULL AUTO_INCREMENT,
                        valeur_depense DECIMAL(10,2) NOT NULL,
                        date_depense DATETIME NOT NULL,
                        etat INT NOT NULL,
                        lead_id INT unsigned DEFAULT NULL,
                        ticket_id INT unsigned DEFAULT NULL,
                        PRIMARY KEY(depense_id),
                        FOREIGN KEY(lead_id) REFERENCES trigger_lead(lead_id),
                        FOREIGN KEY(ticket_id) REFERENCES trigger_ticket(ticket_id)
);


CREATE TABLE seuil(
                      seuil_id INT unsigned NOT NULL AUTO_INCREMENT,
                      taux DECIMAL(10,2) NOT NULL,
                      date_seuil DATETIME NOT NULL,
                      PRIMARY KEY(seuil_id)
);

CREATE OR REPLACE VIEW vue_statistique_budget AS
SELECT
    c.customer_id,
    c.name AS customer_name,
    COALESCE(SUM(b.valeur), 0) AS total_budget,
    COALESCE(d.total_depense_ticket, 0) AS total_depense_ticket,
    COALESCE(d.total_depense_lead, 0) AS total_depense_lead,
    COALESCE(d.total_depense_ticket, 0) + COALESCE(d.total_depense_lead, 0) AS total_depense,
    (COALESCE(SUM(b.valeur), 0) - (COALESCE(d.total_depense_ticket, 0) + COALESCE(d.total_depense_lead, 0))) AS budget_restant
FROM customer c
LEFT JOIN budget b ON c.customer_id = b.customer_id
LEFT JOIN (
    -- Pré-agrégation des dépenses séparées par tickets et leads
    SELECT 
        COALESCE(t.customer_id, l.customer_id) AS customer_id,
        SUM(CASE WHEN t.customer_id IS NOT NULL THEN d.valeur_depense ELSE 0 END) AS total_depense_ticket,
        SUM(CASE WHEN l.customer_id IS NOT NULL THEN d.valeur_depense ELSE 0 END) AS total_depense_lead
    FROM depense d
    LEFT JOIN trigger_ticket t ON d.ticket_id = t.ticket_id
    LEFT JOIN trigger_lead l ON d.lead_id = l.lead_id
    GROUP BY COALESCE(t.customer_id, l.customer_id)
) d ON c.customer_id = d.customer_id
GROUP BY c.customer_id, c.name, d.total_depense_ticket, d.total_depense_lead;

CREATE VIEW v_lead_depense AS
SELECT 
    l.lead_id,
    l.customer_id,
    l.user_id,
    l.name,
    l.phone,
    l.employee_id,
    l.status,
    l.meeting_id,
    l.google_drive,
    l.google_drive_folder_id,
    l.created_at AS lead_created_at,
    d.depense_id,
    d.valeur_depense,
    d.date_depense,
    d.etat,
    d.ticket_id
FROM trigger_lead l
LEFT JOIN depense d ON l.lead_id = d.lead_id;

CREATE VIEW v_ticket_depense AS
SELECT 
    t.ticket_id,
    t.subject,
    t.description,
    t.status,
    t.priority,
    t.customer_id,
    t.manager_id,
    t.employee_id,
    t.created_at,
    d.depense_id,
    d.valeur_depense,
    d.date_depense,
    d.etat,
    d.lead_id
FROM trigger_ticket t
LEFT JOIN depense d ON t.ticket_id = d.ticket_id;


CREATE VIEW v_depense_stat AS
SELECT 
    COALESCE(SUM(CASE WHEN lead_id IS NOT NULL THEN valeur_depense END), 0) AS total_lead_depense,
    COALESCE(SUM(CASE WHEN ticket_id IS NOT NULL THEN valeur_depense END), 0) AS total_ticket_depense,
    COALESCE(SUM(valeur_depense), 0) AS total_depense,
    ROUND((COALESCE(SUM(CASE WHEN lead_id IS NOT NULL THEN valeur_depense END), 0) / 
           COALESCE(SUM(valeur_depense), 1)) * 100, 2) AS pourcentage_lead,
    ROUND((COALESCE(SUM(CASE WHEN ticket_id IS NOT NULL THEN valeur_depense END), 0) / 
           COALESCE(SUM(valeur_depense), 1)) * 100, 2) AS pourcentage_ticket
FROM depense;
