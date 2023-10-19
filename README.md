<p align="center">
  <img alt="PINcredible" src="./app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120"/>
</p>

<h1 align="center">Velociraptor V2</h1>

<p align="center">
    <font size="+1">Fork of the </font><a href="https://github.com/pluscubed/velociraptor"><font size="+1">original Velociraptor by 
Daniel Ciao (pluscubed)</font></a>
</p>

[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://apilevels.com)
[![release](https://img.shields.io/github/release/cyb3rko/velociraptor-v2.svg)](https://github.com/cyb3rko/velociraptor-v2/releases/latest)
[![license](https://img.shields.io/github/license/cyb3rko/velociraptor-v2)](https://www.gnu.org/licenses/gpl-3.0.en.html)
[![last commit](https://img.shields.io/github/last-commit/cyb3rko/velociraptor-v2?color=F34C9F)](https://github.com/cyb3rko/velociraptor-v2/commits/main)

---

<p align="center">Floating speed limit monitor for Android with speedometer and warning features.<br/>
<a href="https://github.com/cyb3rko/velociraptor-v2/releases/latest">
<img height="80" src="https://raw.githubusercontent.com/gotify/android/master/download-badge.png"/>
<a/>
</p>

---

## How does it work?

Every single second your location is updated. If you are more than 10 meters away from your last location where the speed limit was checked, a new request will be created.  
The service provider is https://overpass.kumi.systems.

The app updates the speed limit by requesting all streets in a radius of 15 meters around your location.

<img alt="PINcredible" src="https://i.imgur.com/4N0Owkq.jpg"/>

The API then returns the streets including their tags and speed limits (see the blue streets).

<img alt="PINcredible" src="https://i.imgur.com/E55RL2m.jpg"/>

Now the app tries to find the best match and load it into the speedometer.

## License

```
Copyright (C) 2023 Cyb3rKo

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
```
