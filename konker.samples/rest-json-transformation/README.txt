Data transformation sample application

It reads JSON from the POST data and apply some transformations based on the URL used. The transformation is logged and can bew viewed on http://host:port/transform/log. Last 10 transformations are listed.

Some usages:

  - Remove Fields
  - Add Fields
  - Randomize Data
  - Empty Documents
  - Forcing Error


=== REMOVE FIELDS ===
curl -H 'Content-type: application/json' -d '{"a": 1, "b": 2}' http://host:5565/transform/remove/field/a

Result:
{
  "b": 2
}

curl -H 'Content-type: application/json' -d '{"a": 1, "b": 2}' http://host:5565/transform/remove/field/b

Result:
{
  "a": 1
}

=== ADD FIELDS ===
Adding a Text Field:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/add/field/c/3

Result: 
{
  "a": 1, 
  "b": 2, 
  "c": "3"
}

Adding an Integer Field
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/add/field/c/3/int

Result:
{
  "a": 1, 
  "b": 2, 
  "c": 3
}

Replacing a Field:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/add/field/b/3/int

{
  "a": 1, 
  "b": 3
}

=== RANDOM DATA ===
Adding a Random Field
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/randomize/field

Result:
{
  "a": 1, 
  "b": 2, 
  "diengxbq": "fkdga"
}

Adding a Field with Random Text
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/randomize/field/c

Result:
{
  "a": 1, 
  "b": 2, 
  "c": "pgorb"
}

Adding a Field with Random Number
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/randomize/field/c/int

Result:
{
  "a": 1, 
  "b": 2, 
  "c": 7
}


Adding a Field with Random Text with size between 5 and 8
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/randomize/field/c/string/5/8

Result:
{
  "a": 1, 
  "b": 2, 
  "c": "zlnbyek"
}

Adding a Field with Random Number between 20 and 25
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/randomize/field/c/int/20/25

Result:
{
  "a": 1, 
  "b": 2, 
  "c": 23
}

Creating a complete random document
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/randomize/document

Result:
{
  "grcndomw": {
    "frgnpctv": 1
  }, 
  "iryxtmgq": "vbs", 
  "lgzvwbca": 2, 
  "prfhbusi": "hwovberypn", 
  "pvzeqnhg": "ed"
}

=== EMPTY DOCUMENTS ===
An empty (zero sized) response:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/empty/response

Result: Nothing (zero sized response)

An empty JSON document:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/empty/document

Result:
{}

An empty JSON list:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/empty/list

Result:
[]

=== FORCING AN ERROR ===
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host:5565/transform/abort

Result:
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<title>400 Bad Request</title>
<h1>Bad Request</h1>
<p>Aborted</p>


