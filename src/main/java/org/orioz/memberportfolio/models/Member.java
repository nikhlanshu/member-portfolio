package org.orioz.memberportfolio.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orioz.memberportfolio.dtos.MemberRegistrationRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// @Document annotation tells Spring Data MongoDB to map this class to a MongoDB collection.
// The 'collection' attribute specifies the name of the collection (defaults to class name lowercase if not specified).
@Document(collection = "members")
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
public class Member {

    // @Id annotation marks this field as the primary identifier for the MongoDB document.
    // Spring Data MongoDB will automatically handle its generation (ObjectId) if not set.
    @Id
    private String id;

    private String firstName;
    private String lastName;

    // @Indexed(unique = true) creates a unique index on the 'email' field in MongoDB,
    // ensuring no two members can have the same email address.
    @Indexed(unique = true)
    private String email;

    // IMPORTANT: In a real application, this password MUST be hashed before saving.
    // It should NEVER be stored in plain text.
    private String password;

    private LocalDate dateOfBirth;

    // This is an array of embedded documents in MongoDB. Each element in the 'addresses' list
    // will be a sub-document within the main Member document.
    private List<AddressInfo> addresses;

    // Similar to addresses, this is an array of embedded contact information sub-documents.
    private List<ContactInfo> contacts;

    private String occupation;
    private String profilePictureUrl;

    // Boolean field to indicate lifetime membership status
    private boolean isLifetimeMember = false; // Default value

    // Embedded document for lifetime membership details.
    // This will be stored as a sub-document directly within the Member document if not null.
    private MembershipDetails membershipDetails;

    // Timestamp when the member registered, typically set by the application.
    private LocalDateTime memberSince = LocalDateTime.now(); // Default value

    private LocalDateTime lastLogin; // Last login timestamp

    // List of roles (e.g., "MEMBER", "ADMIN"). Stored as an array of strings in MongoDB.
    private List<Role> roles;

    // List of embedded documents representing events the member has registered for.
    private List<RegisteredEvent> registeredEvents;

    // Flexible field for various user preferences, stored as a BSON object (Map) in MongoDB.
    private Map<String, Object> preferences;

    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Nested Embedded Document Classes ---

    // These static nested classes define the structure of sub-documents embedded
    // within the main Member document. They do NOT get their own separate collections.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String type; // e.g., "LOCAL", "OVERSEAS", "MAILING"
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String postCode; // For international postal codes
        private String province; // For regions like Canadian provinces
        private String country;
        private boolean primary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private String type; // e.g., "LOCAL_PHONE", "OVERSEAS_PHONE", "EMAIL", "WHATSAPP"
        private String value; // The actual phone number, email address, etc.
        private String method; // Categorization: "PHONE", "EMAIL", "MESSAGING_APP"
        private boolean primary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipDetails {
        private String paymentTransactionId;
        private LocalDateTime paymentDate;
        private double amountPaid;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisteredEvent {
        private String eventId; // Reference to an Event document (by ID)
        private LocalDateTime registrationDate;
    }

    public enum Status {
        PENDING, CONFIRMED, REJECTED
    }

    public enum Role {
        MEMBER, ADMIN, FINANCE, PRIME
    }

    public static Member toMember(MemberRegistrationRequest request, PasswordEncoder passwordEncoder) {
        if (request == null) {
            return null;
        }
        Member member = new Member();
        // Fields directly from the request DTO
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword())); // !!! IMPORTANT: This password needs to be HASHED !!!
        member.setDateOfBirth(request.getDateOfBirth());

        // Ensure lists are not null, create new ones if necessary
        member.setAddresses(request.getAddresses() != null ? new ArrayList<>(request.getAddresses()) : new ArrayList<>());
        member.setContacts(request.getContacts() != null ? new ArrayList<>(request.getContacts()) : new ArrayList<>());

        member.setOccupation(request.getOccupation());
        member.setProfilePictureUrl(request.getProfilePictureUrl());
        // Set default values for fields not provided in registration request
        member.setId(null); // ID will be generated by MongoDB
        member.setLifetimeMember(false); // New members are not lifetime members by default
        member.setMemberSince(LocalDateTime.now()); // Set registration timestamp
        member.setLastLogin(null); // No login yet
        member.setRoles(Collections.singletonList(Role.MEMBER)); // Assign default role
        member.setRegisteredEvents(new ArrayList<>()); // No events registered yet
        member.setPreferences(Collections.emptyMap()); // No preferences set yet
        member.setMembershipDetails(null); // Not a lifetime member yet
        member.setStatus(Status.PENDING); // when registered the status will be always pending.
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        return member;
    }
}