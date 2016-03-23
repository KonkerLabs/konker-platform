import requests
import xml.etree.ElementTree as ET

ns = {'real_person': 'http://people.example.com',
      'role': 'http://characters.example.com'}

def get_customer(email):
    url = "http://ec2-52-4-244-64.compute-1.amazonaws.com/konkershop/api/customers?filter[email]=%s" % email
    r = requests.get(url, auth=('W79GDVSI32LLJA8BVVZGSYLUAX7FJCHB', ''))
#    print r.text
    xml = ET.fromstring(r.text)
    if len(xml.find(".//customers").getchildren()) == 0:
        print "No customers"
    else:
        print "Found: %s" % xml.find(".//customer[0]").attrib['id']
#    print xml.find(".//customer[1]").attrib['id']