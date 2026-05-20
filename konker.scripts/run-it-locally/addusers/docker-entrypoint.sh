#!/bin/sh

set -e

echo ""
echo ""
echo "##################################### Konker Open Platform #######################################"
echo "##                                       Version: 0.9.1                                         ##"
echo "##                                  Release date: 2019-01-17                                    ##"
echo "##              Licence: Apache V2 (http://www.apache.org/licenses/LICENSE-2.0)                 ##"
echo "##                           Need Support?: support@konkerlabs.com                              ##"
echo "##################################################################################################"
echo ""
echo ""
echo "hhhhhhhhhhhhhhhhhhhhhyyyyyys/' "
echo "hhhhhhhhhhhhhhhhhhhyyyyyys+.  "
echo "hhhhhhhhhhhhhhhhhhyyyyyyo-   "
echo "hhhhhhhhhhhhhhhhyyyyyys:'    +hy                         sho"
echo "hhhhhhhhhhhhhhyyyyyys+'      +hy '/o+'./oooo/. .oo:+os+- sho '+o/'-+ooo+- -o/:os"
echo "hhhhhhhhhhhhhyyyyyyo-        +hy.sh+'-yh/..:yh/-hho..+hh'sho-yh/'/hs---yh::hho:-"
echo "hhhhhhhhhhhhyyyyyyo'         +hyyh+  ohy    ohy-hh.  -hh.shyhh/  yhsoooss+:hh'"
echo "hhhhhhhhhdddhyyyyyhs-        +hy.sho.-yh/..:hh/-hh'  -hh.sho-yh+./hy-..-:':hh"
echo "hhhhhhhdddddddhyhhhhy+.      :o+  :o+-./oooo/. .oo'  .oo'/o: '/o+.-+oooo+ -oo"
echo "hhhhhdddddddddddhhhhhhy/'"
echo "hhhhddddddddddddddhhhhhhs-"
echo "hhdddddddddddddddddhhhhhhyo."
echo "dddddddddddddddddddddhhhhhhy/'                                                  "
echo ""
echo ""

#Set database version
/usr/bin/update_database.py

#Set default user
echo "populating konker database..."
#/usr/bin/populate_demo_data_roles.py
# python /usr/bin/populate_users.py
# echo "migrate roles..."
# python /usr/bin/userskonker/migrate_user_roles.py

# Check if users.txt exists
if [ ! -f "/users.txt" ]; then
    echo "ERROR: users.txt file not found!"
    exit 1
fi

echo "DEBUG: Found users.txt file"
echo "DEBUG: File size: $(wc -c < users.txt) bytes"
echo "FILE CONTENTS: $(cat users.txt)"

# Read and process users.txt
echo "DEBUG: Starting to iterate over users..."
echo "=========================================="

user_count=0
results=""

while read -r line; do
    echo "$line" | tr ',' '\n' | while read -r user; do
        # Trim whitespace
        user=$(echo "$user" | xargs)

        # Skip empty values
        [ -z "$user" ] && continue

        user_count=$((user_count + 1))

        # Generate random 8-character password (letters + numbers only)
        password=$(tr -dc 'A-Za-z0-9' </dev/urandom | head -c 8)

        echo "DEBUG: Processing user #$user_count: $user"

        # Create user
        python /usr/bin/dsl.py user create --org="$user" "$user" "$password"

         # Print final result
        echo "CREATED: $user -> $password"
        echo "-----------------------------------"

        # Store result
        results="${results}${user} -> ${password}\n"
    done
done < users.txt

# Print all results together at the end
echo ""
echo "=========== CREATED USERS ==========="
printf "%b" "$results"
echo "====================================="

echo "=========================================="
echo "DEBUG: Finished iterating over users"
echo "DEBUG: Total users processed: $user_count"
echo ""

echo "=========================================="
echo "Docker Entrypoint Script Completed Successfully"
echo "=========================================="


exec "$@"
