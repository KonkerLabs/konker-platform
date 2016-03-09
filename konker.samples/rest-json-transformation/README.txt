Data Transformation Sample
==========================

Manipulates JSON objects received by POST data. The recent log of requests and responses are available at http://host/admin/log. Last 10 transformations are listed.

This usage document is avaialble at http://host/admin/help

Some usages:

  - Remove Fields
  - Change Value of Fields
  - Select only one field
  - Create new Fields
  - Randomize Data
  - Empty Documents
  - Forcing Error

The general URL pattern is:

/document/<doc_base>/<field_name>/<operation>?value=x&type=y&random_name=true&random_value=true&min=0&max=10

Where doc could be 
  - empty  (an empty document)
  - random (a random generated document)
  - current (the document received by POST)

If field_name is not define, like /document/random , the value returned is the entire document.

If field_name is defined but operation is not define, the value returned is a document containing only the selected field.

Operations are remove and set.
Types are int, string and json.

There are also

/literal/empty_list  => Returns an empty json list
/literal/empty_text  => Returns an empty text
/literal/abort       => Returns a 400 Error

Same examples:

=== REMOVE FIELDS ===
curl -H 'Content-type: application/json' -d '{"a": 1, "b": 2}' http://host/document/current/a/remove

Result:
{
  "b": 2
}

curl -H 'Content-type: application/json' -d '{"a": 1, "b": 2}' http://host/document/current/b/remove

Result:
{
  "a": 1
}

=== CHANGE AND ADD FIELDS ===
Adding a Text Field:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host/document/current/c/set?value=3


Result: 
{
  "a": 1, 
  "b": 2, 
  "c": "3"
}

Setting an Integer Field
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/c/set?value=3&type=int'

Result:
{
  "a": 1, 
  "b": 2, 
  "c": 3
}

Replacing a Field:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/b/set?value=3&type=int'

Result:
{
  "a": 1, 
  "b": 3
}

Generate a dcument containing only one field:
curl -H 'Content-type: application/json' 'http://host/document/empty/x/set?value=10&type=int'

Result:
{
  "x": 10
}

=== RANDOM DATA ===
Adding a Field with Random Text
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/c/set?random_value=true'

Result:
{
  "a": 1, 
  "b": 2, 
  "c": "pgorb"
}

Adding a Field with Random Text with size between 5 and 8
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/c/set?random_value=true&min=5&max=8'

Result:
{
  "a": 1, 
  "b": 2, 
  "c": "zlnbyek"
}



Adding a Field With Random Name
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/c/set?value=foo&random_name=true'

Result:
{
  "a": 1, 
  "b": 2, 
  "diengxbq": "foo"
}



Adding a Field with Random Number
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/c/set?random_value=true&type=int'

Result:
{
  "a": 1, 
  "b": 2, 
  "c": 7
}


Adding a Field with Random Number between 20 and 25
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' 'http://host/document/current/c/set?random_value=true&type=int&min=20&max=25'

Result:
{
  "a": 1, 
  "b": 2, 
  "c": 23
}

Creating a complete random document
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host/document/random

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
 '{"a": 1, "b": 2}' http://host/literal/empty_response

Result: Nothing (zero sized response)

An empty JSON document:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host/document/empty

Result:
{}

An empty JSON list:
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host/literal/empty_list

Result:
[]

=== FORCING AN ERROR ===
curl -H 'Content-type: application/json' -d
 '{"a": 1, "b": 2}' http://host/literal/abort

Result:
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<title>400 Bad Request</title>
<h1>Bad Request</h1>
<p>Aborted</p>


