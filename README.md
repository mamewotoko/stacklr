stacklr - Stack based shopping/TODO list
========================================
Concept
-------
<pre>
  +----------
  | Apple   ^
  | Orange  | shopping list
  =========== 
  | Banana  | stock
  ===========
  | Sugar   V history: candidates to add to shopping list 
  +---------
</pre>

Functions
---------
- add new item to shopping list
   by keyboard of smart phone
   by voice
   from pc
- move shopping list to stock
   bought item but not consumed
- move stock to shopping list
   no stock
- move history item to shopping list
- add timestamp to each list item
- undo?
- search item?
- recommend new item?
- customize push order
    simple list
    sort by frequency of touch
    ...
- support due date

Build
-----
- copy google-play-service_lib code into libsrc
  TODO: write detail
- add libsrc/google-play-services_lib as library project
- build using ant
```bash
ant debug
```

TODO
-----
- update google task
- add loading icon
- add sync config
- add clear menu
- add touch menu, which is just update timestamp
- sync items with google tasks
   get list from google
   merge list
   update list
- use new menu design
- add last update time
- draw icon
- add dialog to set item property
   item type
   button to select next group
   color?
- add TODO group
   add recipe
   add items to consume
- add licence file
- add wording file and support multiple language
- history list
  add timestamp
- add "stock" group?
- UI with Action bar
  http://developer.android.com/guide/topics/ui/actionbar.html
- implement "push to history list"
    add checkbox to stack item
    use swipe to remove?
      https://github.com/47deg/android-swipelistview
      http://www.tutecentral.com/android-swipe-listview/
- implement "enter text by speech"
- add items in history as suggestion
- Data storage design
  Write data storage interface
    integrate with cursor adapter?
  Support multiple storage
    e.g. Google tasks API
    e.g. Redmine ticket
    e.g. Github issue?
  Save items to local database
  Sync with google tasks/clendar/spreadsheet?
- localize
   English
   Japanese
   Chinese?
- 2 listview UI (old)
  change height correspnding to number of stack items

MEMO
----
- Google Keep has no api yet
  http://stackoverflow.com/questions/19196238/is-there-a-google-keep-api
- Google Calendar API
  https://developers.google.com/google-apps/calendar/?hl=ja
- Google Tasks API
  https://developers.google.com/google-apps/tasks/?hl=ja
  - Authorizing with Google for REST APIs
    http://developer.android.com/google/auth/http-auth.html
  https://developers.google.com/accounts/docs/OAuth2
  - Tasks API v1 (Rev. 41) 1.20.0
    https://developers.google.com/resources/api-libraries/documentation/tasks/v1/java/latest/overview-summary.html
    - Sample code (mercurial repository)
      https://code.google.com/p/google-api-java-client.samples/

- Add Google Play Services to Your Project
  https://developer.android.com/google/play-services/setup.html#Setup
Tasks API Client Library for Java - Google APIs Client Library for Java — Google Developers
- Class Tasks
https://developers.google.com/resources/api-libraries/documentation/tasks/v1/java/latest/com/google/api/services/tasks/Tasks.html

- initialize android project

android create project -n stacklr -p . -t android-16 -k com.mamewo.stacklr -a StacklrActivity --subprojects --library libsrc/google-play-services_lib
cd libsrc/google-play-services_lib
android update lib-project -p . -t android-16
- This code include copies of tasks-android-sample
    https://code.google.com/p/google-api-java-client.samples/