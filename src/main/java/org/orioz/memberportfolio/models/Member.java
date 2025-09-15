package org.orioz.memberportfolio.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.UpdateMemberRequest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member implements Persistable<String> {
    @Id
    private String id;

    private String userId;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String password;

    private LocalDate dateOfBirth;

    // This is an array of embedded documents in MongoDB. Each element in the 'addresses' list
    // will be a sub-document within the main Member document.
    private List<AddressInfo> addresses;

    // Similar to addresses, this is an array of embedded contact information sub-documents.
    private List<ContactInfo> contacts;

    private String occupation;
    private String profilePictureUrl;

    // Embedded document for lifetime membership details.
    // This will be stored as a sub-document directly within the Member document if not null.
    private MembershipDetails membershipDetails;

    // Timestamp when the member registered, typically set by the application.
    private LocalDate registeredSince; // when the member is confirmed

    private LocalDateTime lastLogin; // Last login timestamp

    // List of roles (e.g., "MEMBER", "ADMIN"). Stored as an array of strings in MongoDB.
    private List<Role> roles;

    // List of embedded documents representing events the member has registered for.
    private List<RegisteredEvent> registeredEvents;

    // Flexible field for various user preferences, stored as a BSON object (Map) in MongoDB.
    private Map<String, Object> preferences;

    private Status status;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public boolean isNew() {
        return createdAt == null;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String type;
        private String street;
        private String suburb;
        private String city;
        private String state;
        private String postCode;
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
        private MembershipDuration duration;
        private LocalDate startDate;
        private Double amount;
    }

    public enum MembershipDuration {
        YEARLY,
        MONTHLY,
        LIFETIME,
        NONE
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
        MEMBER, ADMIN, FINANCE
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
        member.setUserId(extractUserId(request.getEmail()));
        member.setPassword(passwordEncoder.encode(request.getPassword())); // !!! IMPORTANT: This password needs to be HASHED !!!
        member.setDateOfBirth(request.getDateOfBirth());

        // Ensure lists are not null, create new ones if necessary
        member.setAddresses(request.getAddresses() != null ? new ArrayList<>(request.getAddresses()) : new ArrayList<>());
        member.setContacts(request.getContacts() != null ? new ArrayList<>(request.getContacts()) : new ArrayList<>());

        member.setOccupation(request.getOccupation());
        member.setProfilePictureUrl(request.getProfilePictureUrl());
        // Set default values for fields not provided in registration request
        member.setId(UUID.randomUUID().toString());
        member.setLastLogin(null); // No login yet
        member.setRoles(Collections.singletonList(Role.MEMBER)); // Assign default role
        member.setRegisteredEvents(new ArrayList<>()); // No events registered yet
        member.setPreferences(Collections.emptyMap()); // No preferences set yet
        member.setMembershipDetails(null); // Not a lifetime member yet
        member.setStatus(Status.PENDING); // when registered the status will be always pending.
        return member;
    }

    public static String extractUserId(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0) { // no @ or starts with @
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        return email.substring(0, atIndex).trim().toLowerCase();
    }
    public static Member updateMember(UpdateMemberRequest source, Member destination) {
        // Convert existing addresses and contacts to maps for easy lookup
        Map<String, Member.AddressInfo> addressMap = destination.getAddresses().stream()
                .collect(Collectors.toMap(addr -> addr.getType().toUpperCase(), addr -> addr));

        Map<String, Member.ContactInfo> contactMap = destination.getContacts().stream()
                .collect(Collectors.toMap(c -> c.getType().toUpperCase(), c -> c));

        // Update addresses
        if (source.getAddresses() != null) {
            for (Member.AddressInfo incoming : source.getAddresses()) {
                String typeKey = incoming.getType().toUpperCase();
                Member.AddressInfo existing = addressMap.get(typeKey);
                if (existing != null) {
                    updateAddressFields(existing, incoming);
                } else {
                    destination.getAddresses().add(incoming);
                    addressMap.put(typeKey, incoming);
                }
            }
        }

        // Update contacts
        if (source.getContacts() != null) {
            for (Member.ContactInfo incoming : source.getContacts()) {
                String typeKey = incoming.getType().toUpperCase();
                Member.ContactInfo existing = contactMap.get(typeKey);
                if (existing != null) {
                    existing.setValue(incoming.getValue());
                    existing.setPrimary(incoming.isPrimary());
                } else {
                    destination.getContacts().add(incoming);
                    contactMap.put(typeKey, incoming);
                }
            }
        }

        return destination;
    }
    private static void updateAddressFields(Member.AddressInfo existing, Member.AddressInfo incoming) {
        existing.setStreet(incoming.getStreet());
        existing.setSuburb(incoming.getSuburb());
        existing.setCity(incoming.getCity());
        existing.setState(incoming.getState());
        existing.setPostCode(incoming.getPostCode());
        existing.setCountry(incoming.getCountry());
        existing.setPrimary(incoming.isPrimary());
    }
}