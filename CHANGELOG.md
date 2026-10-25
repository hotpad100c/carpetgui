# 2.1.0

### Fixes

* **Lag when searching with many rules** ([#21](https://github.com/hotpad100c/carpetgui/issues/21)): rule rows are now built once and reused. Typing in the search box only updates highlights and re-mounts the matching rows, instead of rebuilding every widget on each keystroke.
* **Enum rule values recorded with changed case** ([#20](https://github.com/hotpad100c/carpetgui/issues/20)): values such as `false` and `FALSE` no longer count as a change, so the rule stack stops recording spurious entries.
* **Hotkey does nothing on some servers** ([#11](https://github.com/hotpad100c/carpetgui/issues/11)): when the server does not have CarpetGUI installed and there is no local cache, the screen is now built from the rules Carpet syncs. If no rules are available at all, an actionbar message explains why the screen cannot open.
* **Crash when joining a server on 1.21.5** ([#5](https://github.com/hotpad100c/carpetgui/issues/5)): added a dedicated 1.21.5 build, since 1.21.5 changed the `CompoundTag` API. The 1.21.4 build is now limited to 1.21.4.
* **Search box shows a caret but ignores typing** ([#22](https://github.com/hotpad100c/carpetgui/issues/22)): the search box now gets real keyboard focus when the rules screen opens, so you can type without clicking it first.
* **Click sound while typing in the search box** ([#23](https://github.com/hotpad100c/carpetgui/issues/23)): typing no longer plays a button click on every character. With an IME, committing several characters at once used to stack the sound.
* **Watchdog crash when quitting after opening the screen** ([#24](https://github.com/hotpad100c/carpetgui/issues/24)): the background thread that saves the rule cache no longer keeps the game process alive, so the game exits normally instead of hanging until the shutdown watchdog kills it.

### Features

* **Configurable menu size and combined filters** ([#7](https://github.com/hotpad100c/carpetgui/issues/7), [#25](https://github.com/hotpad100c/carpetgui/issues/25)):
  * A new settings panel adjusts UI scale (50%–150%), menu width, menu height and category panel width.
  * Hold Shift or Ctrl while clicking categories to combine several filters, for example *modified* + a category. Only rules that match every selected filter are shown.
