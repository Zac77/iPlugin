<?php
/*
 * Description: This file is used to delete a plugin for a user.
 * Author: Zac (Qi ZHANG)
 * Create Date: 10/02/2014
*/
$filename = end(explode("/", __FILE__));
include 'config_db.php';

if (isset($_POST["email"]) && isset($_POST["auth"]) && isset($_POST["deviceID"]) && isset($_POST["sensorID"])) {
	$dbhandle = connectToDB();
	$userID = $dbhandle->query("SELECT id FROM user WHERE email='".$_POST["email"]."' AND auth='".$_POST["auth"]."'");
	$userID = $userID->fetch_array(MYSQLI_ASSOC);
	if (empty($userID)) {
		$dbhandle->close();
		echo "ERROR: User authorization failed.";
	} else {
		$pluginID = $dbhandle->query("SELECT id FROM plugin WHERE board_device_id='".$_POST["deviceID"]."' AND sensor_id='".$_POST["sensorID"]."'");
		$pluginID = $pluginID->fetch_array(MYSQLI_ASSOC);
		if (empty($pluginID)) {
			$dbhandle->close();
			echo "ERROR: Get plugin ID failed.";
		} else {
			if ($dbhandle->query("DELETE FROM user_has_plugin WHERE user_id=".$userID["id"]." AND plugin_id=".$pluginID["id"].""))
				echo "Successfully deleted.";
			else
				echo "ERROR: Failed to delete data from database.";
			$dbhandle->close();
		}
	}
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
