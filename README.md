stacklr - Stack based shopping/TODO list
========================================
Concept
-------
### UI
<pre>
  +----------
  | Apple   ^
  | Orange  | items to buy 
  =========== border
  | Banana  | history 
  | Sugar   V
  +---------
</pre>

### add to stack

<pre>
  +---------+
  | Apple   |
  | Orange  |
  ===========
  | Banana  | &lt;- touch
  | Sugar   |
  +---------+
</pre>
<pre>
  +---------+
  | Banana  | &lt;- push 
  | Apple   |
  | Orange  |
  ===========
  | Sugar   |
  +---------+
</pre>

### pop from stack

<pre>
  +---------+
  | Apple   |
  | Orange  | &lt;- touch
  ===========
  | Banana  | 
  | Sugar   |
  +---------+
</pre>

<pre>
  +---------+
  | Apple   |
  ===========
  | Banana  | 
  | Sugar   |
  | Orange  | &lt;- push? 
  +---------+
</pre>

Functions
---------
- pop
- push
- enter new item to list
   from app
   from pc
- add timestamp to each list item
- undo?
- search item?
- recommend new item?
- customize push order
    simple list
    sort by frequency of touch
    ...
- support due date

TODO
-----
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

- Add Google Play Services to Your Project
  https://developer.android.com/google/play-services/setup.html#Setup
Tasks API Client Library for Java - Google APIs Client Library for Java — Google Developers
- Class Tasks
https://developers.google.com/resources/api-libraries/documentation/tasks/v1/java/latest/com/google/api/services/tasks/Tasks.html

- initialize android project



android create project -n stacklr -p . -t android-16 -k com.mamewo.stacklr -a StacklrActivity --subprojects --library libsrc/google-play-services_lib
cd libsrc/google-play-services_lib
android update lib-project -p . -t android-16
