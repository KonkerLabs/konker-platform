db.createUser(
    {
        user: "user",
        pwd: "password",
        roles: [
            {
                role: "readWrite",
                db: "konker"
            },
            {
                role: "readWrite",
                db: "logs"
            }
        ]
    }
);

// db.createCollection('logs')
// db.createCollection('konker')