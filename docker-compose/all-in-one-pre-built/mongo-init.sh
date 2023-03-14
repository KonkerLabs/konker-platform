set -e

mongo <<EOF
db = db.getSiblingDB('registry');

db.createUser({
    user: 'konker',
    pwd: 'Konker@2023',
    roles: [{ role: 'dbOwner', db:'registry'}, { role: 'dbOwner', db:'logs'}]
});

db = db.getSiblingDB('logs');

db.createUser({
    user: 'logs',
    pwd: 'Konker@2023',
    roles: [{ role: 'dbOwner', db:'logs'}]
});

EOF