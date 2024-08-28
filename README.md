**Simple Regions**

Using MySQL for remote storage, this plugin allows the administrators to define a region of the world. This region can have flags applied to it, so that they may limit entity actions inside of the region. Highly flexible with API for implementing additional flags and generating regions entirely from the RegionAPI class. Future development is already planned due to how expansive it is.

*/region* is the base command to open the gui. The tab-complete should be fully implemented and intuitive to see all the features and their usage.

*/region create <name>*

*/region section add <region> <section name>*

Permissions for internal commands:
- region.menu: opens menu
- region.whitelist: make modifications to the whitelist
- region.create: Manage region and section creation
- region.flag: Modify flags and use flag menu
- region.bypass: Always treated as whitelisted and immune to most region limits
- region.selection: Use the wand to select region areas.

Recommended permissions for MySQL users: "SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES"
