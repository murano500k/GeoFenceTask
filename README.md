# GeoFenceTask
Task for Borys
*Main screen:*
* location status on top: isEnabled, current address, accuracy, mode
* show updated geofences list
* autoremove outdated fences

* floatingActionButton:
add geofence:
1 map screen (including user position with updates)
2 geofence options screen:
name edittext
radius dialog/fragment
location 2 edittexts
transition type checkboxes (multichoice)
expiration time date/time picker
never expire option

*options menu:*
- option1: filters: all, active

- option2: show all/active geofences on map
highlight active, 
highlight when user is inside

- option3: 
gps settings screen 
extends settings activity
location tracking always on 
update interval (app is not open)
min update interval (app is open)
mode(gps, wifi, cellular)

* onlistitemclick
*geofence details screen:*
- time table button (opens time table screen)
- activate fence switch
- button show fence on map
- edit geofence button (point, radius, my location,type checkboxes)
- current status (is active, user inside) indicators
- show notification checkbox, notification options(autohide)
- set ringtone volume when you are inside geofence area switch
- volume select bar
- delete button

*time table screen:
filters
total time

*style:*
- follow android design guidelines
- list items should look like cards
- apply custom theme elements (screenshots attached)
- apply 3 custom animations (gifs attached)
- show progress on all async actions
- use constraint layout
- use cardview 

* notification: 
on click 
- open geofence on map, with parent activity stack: 
MapActivity < GeoFenceDetailsActivity < ListActivity
- show active geofence time in current session
- geofence name
- current status




General
All activities should respond to config changes and keep user data updated. 
List of geofences should update in realtime
