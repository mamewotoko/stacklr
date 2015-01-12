from oauth2client.client import flow_from_clientsecrets, credentials_from_clientsecrets_and_code
from apiclient.discovery import build
import json, httplib2, sys
from oauth2client.file import Storage

config = "../secret.json"
storage_file = "../credentials.dat"

#code="XXX"
credentials = None

#if code is None:
flow = flow_from_clientsecrets(config,
                               scope='https://www.googleapis.com/auth/calendar',
                               redirect_uri='http://localhost')

auth_uri = flow.step1_get_authorize_url()
print auth_uri
http = httplib2.Http()
resp, content = http.request(auth_uri, method='GET')
#print content
print "enter code:"
code = sys.stdin.readline().rstrip()
credentials = flow.step2_exchange(code, http=http)
storage = Storage(storage_file)
storage.put(credentials)

# else:
#     credentials = credentials_from_clientsecrets_and_code(config, 
#                                                           scope='https://www.googleapis.com/auth/calendar',
#                                                           code=code,
#                                                           redirect_uri='http://localhost')

http = credentials.authorize(http)

service = build('calendar', 'v3', http=http)
req = service.calendarList().list()
response = req.execute()
print req
print response
