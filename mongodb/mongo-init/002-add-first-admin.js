db = db.getSiblingDB('your_database_name');

const adminExists = db.members.findOne({ roles: "ADMIN" });

if (!adminExists) {
    db.members.insertOne({
        firstName: "Super",
        lastName: "Admin",
        email: "admin@orioz.org",
        password: "$2a$10$X6KMEGgkjr6vXrg4XBZKy.CVqTwXABPZ1fyfFd1/FGXkqgW3QqL8K",
        dateOfBirth: ISODate("1980-01-01T00:00:00Z"),
        addresses: [{
            type: "LOCAL",
            street: "123 Orioz Lane",
            city: "Brisbane",
            state: "QLD",
            zipCode: "4000",
            postCode: "4000",
            province: "",
            country: "Australia",
            primary: true
        }],
        contacts: [{
            type: "EMAIL",
            value: "admin@orioz.org",
            method: "EMAIL",
            primary: true
        }],
        occupation: "System Administrator",
        profilePictureUrl: "https://example.com/images/admin.png",
        isLifetimeMember: false,
        membershipDetails: null,
        memberSince: new Date(),
        lastLogin: null,
        roles: ["ADMIN", "MEMBER"],
        registeredEvents: [],
        preferences: {},
        status: "CONFIRMED",
        createdAt: new Date(),
        updatedAt: new Date()
    });
    print("✅ Default ADMIN user created.");
} else {
    print("ℹ️ ADMIN user already exists, skipping creation.");
}