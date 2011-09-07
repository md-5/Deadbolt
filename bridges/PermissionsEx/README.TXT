DeadboltBridge PermissionsEx
The purpose of this plugin is to bridge functionality between Deadbolt and PermissionsEx
By using [GroupName] on signs, the bridge will query PermissionsBukkit to check if a player is a member that group or any it inherits.
If your group's name is greater than 13 characters, for example AdminsLikeItReallyBig truncate it for the sign as [AdminsLikeItR].
WARNING: "AdminsLikeItReallyBig" and "AdminsLikeItReallyHuge" would both have authorization to use the same block due to the truncation.