package site.easy.to.build.crm.service.importcsv;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.TicketDepense;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.importcsv.CustomerRequestImport;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.EmailTokenUtils;

@Service
public class ImportService {
    
    private LocalDateTime generateRandomDateTime() {
        // Générer une date aléatoire entre 2024-01-01 et 2025-12-31
        LocalDate start = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate end = LocalDate.of(2025, Month.DECEMBER, 31);
        long randomDay = ThreadLocalRandom.current().nextLong(start.toEpochDay(), end.toEpochDay());
        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
        
        // Générer une heure aléatoire
        int hour = ThreadLocalRandom.current().nextInt(0, 24);
        int minute = ThreadLocalRandom.current().nextInt(0, 60);
        int second = ThreadLocalRandom.current().nextInt(0, 60);
        
        return LocalDateTime.of(randomDate.getYear(), randomDate.getMonth(), randomDate.getDayOfMonth(), hour, minute, second);
    }

    @Transactional(rollbackOn = Exception.class)
    public void importCsv(MultipartFile file, CustomerService customerService, char separator,
                         PasswordEncoder passwordEncoder, CustomerLoginInfoService customerLoginInfoService,
                         UserService userService) throws Exception {
        List<Customer> customers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int line = 2;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             @SuppressWarnings("deprecation")
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(separator))) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    if (csvRecord.size() == 2) {
                        Customer customer = new Customer();
                        String nomCustomer = csvRecord.get("customer_name").trim();
                        if (nomCustomer == null || nomCustomer.isEmpty()) {
                            errors.add("Ligne " + line + ": Nom du client manquant");
                            continue;
                        }
                        customer.setName(nomCustomer);
                        
                        String emailCustomer = csvRecord.get("customer_email").trim();
                        if (emailCustomer == null || emailCustomer.isEmpty()) {
                            errors.add("Ligne " + line + ": Email du client manquant");
                            continue;
                        }
                        customer.setEmail(emailCustomer);

                        // Vérifier si le client existe déjà
                        if (customerService.findByEmail(emailCustomer) != null) {
                            errors.add("Ligne " + line + ": Un client avec cet email existe déjà");
                            continue;
                        }

                        // login info
                        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
                        customerLoginInfo.setEmail(emailCustomer);
                        customerLoginInfo.setToken(EmailTokenUtils.generateToken());
                        customerLoginInfo.setPasswordSet(true);
                        customerLoginInfo.setPassword(passwordEncoder.encode("123"));

                        CustomerLoginInfo customerLoginInfo2 = customerLoginInfoService.save(customerLoginInfo);
                        customer.setCustomerLoginInfo(customerLoginInfo2);
                        
                        // Set other default values
                        customer.setPosition("imported");
                        customer.setPhone("0000000000");
                        customer.setAddress("imported");
                        customer.setCity("imported");
                        customer.setState("imported");
                        customer.setCountry("Madgascar");
                        customer.setCreatedAt(generateRandomDateTime());
                        customer.setDescription("imported");

                        User user = userService.findById(53);
                        customer.setUser(user);

                        Customer customer2 = customerService.save(customer);
                        customers.add(customer2);
                    } else {
                        errors.add("Ligne " + line + ": Format de ligne invalide (attendu 2 colonnes)");
                    }
                } catch (Exception e) {
                    errors.add("Ligne " + line + ": " + e.getMessage());
                }
                line++;
            }
        } catch (Exception e) {
            errors.add("Erreur lors de la lecture du fichier: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            throw new Exception("Erreurs lors de l'import:\n" + String.join("\n", errors));
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void readOthercsv(MultipartFile file, CustomerRequestImportService requestImportService, char separator,
                           CustomerService customerService, UserService userService, TicketService ticketService, 
                           LeadService leadService, DepenseService depenseService) throws Exception {
        List<User> users = userService.findByRoles_Name("ROLE_MANAGER");
        List<User> users2 = userService.findByRoles_Name("ROLE_EMPLOYEE");
        users2.addAll(users);
        
        List<String> errors = new ArrayList<>();
        int line = 2;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             @SuppressWarnings("deprecation")
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(separator))) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    if (csvRecord.size() == 5) {
                        int randomNumber = (int)(Math.random() * users.size());
                        int randomNumber2 = (int)(Math.random() * users2.size());

                        String mail = csvRecord.get("customer_email").trim();
                        String subject = csvRecord.get("subject_or_name").trim();
                        String type = csvRecord.get("type").trim();
                        String status = csvRecord.get("status").trim();
                        String expense = csvRecord.get("expense").trim();

                        Customer customer = customerService.findByEmail(mail);
                        if (customer == null) {
                            errors.add("Ligne " + line + ": Client avec email " + mail + " non trouvé");
                            continue;
                        }

                        if (type.equals("lead")) {
                            Lead lead = new Lead();
                            lead.setCustomer(customer);
                            lead.setName(subject);
                            lead.setStatus(status);
                            lead.setCreatedAt(generateRandomDateTime());
                            lead.setPhone("0000000000");
                            lead.setManager(users.get(randomNumber));
                            lead.setEmployee(users2.get(randomNumber2));

                            Lead lead2 = leadService.save(lead);

                            try {
                                expense = expense.replace(',', '.');
                                Double expensed = Double.parseDouble(expense);
                                if (expensed <= 0) {
                                    errors.add("Ligne " + line + ": Valeur de dépense négative");
                                    continue;
                                }

                                Depense depense = new Depense();
                                depense.setLead(lead2);
                                depense.setValeurDepense(expensed);
                                depense.setDateDepense(generateRandomDateTime());
                                depense.setEtat(1);
                                depenseService.saveDepense(depense);
                            } catch (Exception e) {
                                errors.add("Ligne " + line + ": Valeur de dépense invalide: " + expense);
                            }
                        } else if (type.equals("ticket")) {
                            Ticket ticket = new Ticket();
                            ticket.setCustomer(customer);
                            ticket.setSubject(subject);
                            ticket.setStatus(status);
                            ticket.setCreatedAt(generateRandomDateTime());
                            ticket.setManager(users.get(randomNumber));
                            ticket.setEmployee(users2.get(randomNumber2));
                            ticket.setPriority("low");

                            Ticket ticket2 = ticketService.save(ticket);

                            try {
                                expense = expense.replace(',', '.');
                                Double expensedo = Double.parseDouble(expense);
                                if (expensedo <= 0) {
                                    errors.add("Ligne " + line + ": Valeur de dépense négative");
                                    continue;
                                }

                                Depense depense = new Depense();
                                depense.setTicket(ticket2);
                                depense.setValeurDepense(expensedo);
                                depense.setDateDepense(generateRandomDateTime());
                                depense.setEtat(1);
                                depenseService.saveDepense(depense);
                            } catch (Exception e) {
                                errors.add("Ligne " + line + ": Valeur de dépense invalide: " + expense);
                            }
                        } else {
                            errors.add("Ligne " + line + ": Type invalide: " + type);
                        }
                    } else {
                        errors.add("Ligne " + line + ": Format de ligne invalide (attendu 5 colonnes)");
                    }
                } catch (Exception e) {
                    errors.add("Ligne " + line + ": Erreur - " + e.getMessage());
                }
                line++;
            }
        } catch (Exception e) {
            errors.add("Erreur lors de la lecture du fichier: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            throw new Exception("Erreurs lors de l'import:\n" + String.join("\n", errors));
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void readBudgetcsv(MultipartFile file, BudgetService budgetService, char separator,
                            CustomerService customerService) throws Exception {
        List<String> errors = new ArrayList<>();
        int line = 2;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             @SuppressWarnings("deprecation")
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(separator))) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    if (csvRecord.size() == 2) {
                        String customerMail = csvRecord.get("customer_email").trim();
                        String budgetValue = csvRecord.get("Budget").trim();
                        Customer customer = customerService.findByEmail(customerMail);

                        if (customer == null) {
                            errors.add("Ligne " + line + ": Client avec email " + customerMail + " non trouvé");
                            continue;
                        }

                        Budget budget = new Budget();
                        try {
                            budgetValue = budgetValue.replace(',', '.');
                            double value = Double.parseDouble(budgetValue);
                            if (value <= 0) {
                                errors.add("Ligne " + line + ": Valeur de budget négative");
                                continue;
                            }
                            budget.setValeur(value);
                            budget.setCustomer(customer);
                            budget.setDate(generateRandomDateTime());
                            budgetService.save(budget);
                        } catch (Exception e) {
                            errors.add("Ligne " + line + ": Valeur de budget invalide: " + budgetValue);
                        }
                    } else {
                        errors.add("Ligne " + line + ": Format de ligne invalide (attendu 2 colonnes)");
                    }
                } catch (Exception e) {
                    errors.add("Ligne " + line + ": Erreur - " + e.getMessage());
                }
                line++;
            }
        } catch (Exception e) {
            errors.add("Erreur lors de la lecture du fichier: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            throw new Exception("Erreurs lors de l'import:\n" + String.join("\n", errors));
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void ImportMitambatra(MultipartFile file, MultipartFile file2, MultipartFile file3, 
                               CustomerService customerService, char separator,
                               PasswordEncoder passwordEncoder, CustomerLoginInfoService customerLoginInfoService,
                               UserService userService, CustomerRequestImportService requestImportService, 
                               TicketService ticketService, LeadService leadService, 
                               DepenseService depenseService, BudgetService budgetService) throws Exception {
        List<String> globalErrors = new ArrayList<>();

        try {
            importCsv(file, customerService, separator, passwordEncoder, customerLoginInfoService, userService);
        } catch (Exception e) {
            globalErrors.add("Erreurs dans le fichier clients: " + e.getMessage());
        }

        try {
            readBudgetcsv(file2, budgetService, separator, customerService);
        } catch (Exception e) {
            globalErrors.add("Erreurs dans le fichier budgets: " + e.getMessage());
        }

        try {
            readOthercsv(file3, requestImportService, separator, customerService, 
                       userService, ticketService, leadService, depenseService);
        } catch (Exception e) {
            globalErrors.add("Erreurs dans le fichier leads/tickets: " + e.getMessage());
        }

        if (!globalErrors.isEmpty()) {
            throw new Exception("Erreurs lors de l'import:\n" + String.join("\n", globalErrors));
        }
    }
}