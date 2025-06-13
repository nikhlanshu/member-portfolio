var db = db.getSiblingDB('orioz-community'); // Matches spring.data.mongodb.database in application.properties

// Create collections if they don't exist
// Using createCollection explicitly ensures they are created
// You can add validation rules or options here if needed.

db.createUser({
    user: "oriozapp",
    pwd: "oriozappsecret",
    roles: [
        {
            role: "readWrite",
            db: "orioz-community"
        }
    ]
});

db.createCollection('members');
print("Collection 'members' created or already exists.");

db.createCollection('events');
print("Collection 'events' created or already exists.");

db.createCollection('news');
print("Collection 'news' created or already exists.");

db.createCollection('transactions'); // If you decided to use this
print("Collection 'transactions' created or already exists.");