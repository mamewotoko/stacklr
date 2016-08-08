from oauth2client.client import flow_from_clientsecrets, credentials_from_clientsecrets_and_code
from apiclient.discovery import build
import json, httplib2, sys
from oauth2client.file import Storage
import setting, os

credentials = None

storage = Storage(setting.STORAGE_FILE)

if os.path.isfile(setting.STORAGE_FILE):
    credentials = storage.get()

if credentials is None:
    #redirect_uri = 'http://localhost'
    redirect_uri = 'urn:ietf:wg:oauth:2.0:oob'
    flow = flow_from_clientsecrets(setting.CONFIG,
                                   scope='https://www.googleapis.com/auth/calendar',
                                   redirect_uri=redirect_uri)

    auth_uri = flow.step1_get_authorize_url()
    print "open the following URL by web browser"
    print auth_uri
    http = httplib2.Http()
    resp, content = http.request(auth_uri, method='GET')
    #print content
    print "enter code (string following \"code=\" of URL):"
    code = sys.stdin.readline().rstrip()
    credentials = flow.step2_exchange(code, http=http)
    storage.put(credentials)
else:
    http = httplib2.Http()

http = credentials.authorize(http)

service = build('calendar', 'v3', http=http)
req = service.calendarList().list()
response = req.execute()
#print req
print json.dumps(response, indent=True, ensure_ascii=False)
