from oauth2client.client import flow_from_clientsecrets, credentials_from_clientsecrets_and_code
from apiclient.discovery import build
import json, httplib2, sys
from oauth2client.file import Storage

config = "../secret.json"
storage_file = "../credentials.dat"
http = httplib2.Http()

# flow = flow_from_clientsecrets(config,
#                                scope='https://www.googleapis.com/auth/calendar',
#                                redirect_uri='http://localhost')

# code = sys.argv[1]
storage = Storage(storage_file)
credentials = storage.get()
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
