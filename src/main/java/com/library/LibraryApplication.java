package com.library;

import com.library.model.*;
import com.library.repository.BookItemRepository;
import com.library.repository.BookTitleRepository;
import com.library.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("=================================================================");
        System.out.println("  ATHENAEUM LIBRARY MANAGEMENT SYSTEM (SPRING BOOT)");
        System.out.println("=================================================================");
        System.out.println("  REST API Endpoints:");
        System.out.println("  - Catalog Search : GET  http://localhost:8080/api/catalog/search?query=");
        System.out.println("  - Members CRUD   : GET  http://localhost:8080/api/users");
        System.out.println("  - Checkout Loan  : POST http://localhost:8080/api/loans/checkout");
        System.out.println("  - Return Loan    : PUT  http://localhost:8080/api/loans/return");
        System.out.println("  - Hold Reserve   : POST http://localhost:8080/api/reservations");
        System.out.println("  - H2 Web Console : http://localhost:8080/h2-console");
        System.out.println("=================================================================");
    }

    /**
     * Seeds initial library records on application startup if database is empty
     */
    @Bean
    public CommandLineRunner dataInitializer(
            MemberRepository memberRepository,
            BookTitleRepository titleRepository,
            BookItemRepository itemRepository) {
        return args -> {
            if (memberRepository.count() == 0) {
                // 1. Initial Members
                memberRepository.saveAll(List.of(
                    new Member("LIB-1001", "Clara Oswald", "clara.oswald@oxford.ac.uk", AccountStatus.ACTIVE, 1, "United Kingdom"),
                    new Member("LIB-1002", "Marcus Vance", "m.vance@mit.edu", AccountStatus.ACTIVE, 1, "United States"),
                    new Member("LIB-1003", "Elena Rostova", "elena.rostova@ethz.ch", AccountStatus.ACTIVE, 0, "Switzerland"),
                    new Member("LIB-1004", "Kenji Takahashi", "kenji.t@tokyo-ac.jp", AccountStatus.ACTIVE, 0, "Japan"),
                    new Member("LIB-1005", "Sophia Dubois", "sophia.d@sorbonne.fr", AccountStatus.PENDING, 0, "France"),
                    new Member("LIB-1006", "David Miller", "d.miller@edinburgh.ac.uk", AccountStatus.SUSPENDED, 0, "United Kingdom")
                ));

                // 2. Initial Book Titles
                titleRepository.saveAll(List.of(
                    new BookTitle("978-1449373320", "Designing Data-Intensive Applications", "Martin Kleppmann", "Distributed Systems", 2017, "QA76.9 .K55 2017", "The definitive reference guide for distributed reliability, replication logs, and storage engines.", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80"),
                    new BookTitle("978-0134494166", "Clean Architecture: A Craftsman Guide", "Robert C. Martin", "Software Engineering", 2018, "QA76.76 .C65 M37", "Universal architectural principles separating domain business rules from external frameworks.", "https://images.unsplash.com/photo-1532012164546-f432f2e3777f?auto=format&fit=crop&w=600&q=80"),
                    new BookTitle("978-0262510875", "Structure and Interpretation of Computer Programs", "Harold Abelson, Gerald Jay Sussman", "Computer Science", 1996, "QA76.6 .A255", "Foundational MIT masterpiece exploring abstraction layers, functional procedures, and metalinguistic programming.", "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80"),
                    new BookTitle("978-0812968255", "Meditations: The Emperor Handbook", "Marcus Aurelius", "Philosophy", 2002, "B580 .A97 2002", "Private spiritual reflections on stoic duty, internal equanimity, and mortality.", "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?auto=format&fit=crop&w=600&q=80"),
                    new BookTitle("978-1491929124", "Site Reliability Engineering", "Betsy Beyer, Chris Jones, Niall Murphy", "Cloud Architecture", 2016, "TK5105.875 .S58", "Engineering practices pioneered by Google for operating planetary-scale software systems.", "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=600&q=80"),
                    new BookTitle("978-0441569595", "Neuromancer", "William Gibson", "Speculative Fiction", 1984, "PS3557 .I226 N4", "Hugo and Nebula winning novel that introduced cyberspace, AI constructs, and the neon sprawl.", "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=600&q=80")
                ));

                // 3. Initial Physical & Digital Copies (with version = 0)
                itemRepository.saveAll(List.of(
                    new BookItem("BC-1001-A", "978-1449373320", BookStatus.AVAILABLE, "Stack 4 • Shelf A"),
                    new BookItem("BC-1001-B", "978-1449373320", BookStatus.LOANED, "Stack 4 • Shelf A"),
                    new BookItem("BC-1002-A", "978-0134494166", BookStatus.AVAILABLE, "Stack 3 • Shelf C"),
                    new BookItem("BC-1003-A", "978-0262510875", BookStatus.DIGITAL, "Digital Repository"),
                    new BookItem("BC-1004-A", "978-0812968255", BookStatus.LOANED, "Stack 1 • Shelf B"),
                    new BookItem("BC-1005-A", "978-1491929124", BookStatus.RESERVED, "Holds Reserve Shelf"),
                    new BookItem("BC-1006-A", "978-0441569595", BookStatus.AVAILABLE, "Fiction Wing • Row 2")
                ));
            }
        };
    }
}
