# SimpleToDo
To Do:
    Priority scale (1-5)
- app launch screen and app icon(5)
- fragment task list toolbar should have night mode toggle on right, and back up and restore launch on the left side of title (2)
- Backup and restore db (5)

ideas:
- if app has not launched in a while present welcome screen with upcoming tasks
- pull down on list to reveal search
- toggle for reminds(ability to set multiple)
- I want to avoid having a settings page, why? to keep things as minimal as possible.
- multiple reminders
- Launch activity, prompts to sign into google to restore, or restore from sdcard, or start new
- Add timer indicator to indicate task approaching deadline on list item view (3)

Bugs:
- return the list in the same order everytime (related to sql database and temporary commit)
- stop recyclerview scrolling when no items or less than 12 items are loaded

other:
- clean up code.....(android lint warnings etc..)
- mvvm
