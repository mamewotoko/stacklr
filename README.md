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
- implement "push to history list"
    add checkbox to stack item
    use swipe?
      https://github.com/47deg/android-swipelistview
      http://www.tutecentral.com/android-swipe-listview/
- implement "enter text by speech"
- add items in history as suggestion
- Data storage design
  Write data storage interface
  Save items to local database
  Web UI?
  Sync with google tasks/clendar/spreadsheet?

MEMO
----
- Google Keep has no api yet
  http://stackoverflow.com/questions/19196238/is-there-a-google-keep-api
- Google Calendar API
  https://developers.google.com/google-apps/calendar/?hl=ja
- Google Tasks API
  https://developers.google.com/google-apps/tasks/?hl=ja

android create project -n stacklr -p . -t android-16 -k com.mamewo.stacklr -a StacklrActivity
