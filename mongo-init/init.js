db = db.getSiblingDB('kufar')

db.createCollection('test-my-collection')
db.users.insertOne({
    user: "myuser",
        pwd: "mypassword",
        roles: [{ role: "readWrite", db: "kufar" }]
})