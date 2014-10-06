<?php
/*
 * Description: This file is used to add a plugin for a user.
 * Author: Zac (Qi ZHANG)
 * Create Date: 10/02/2014
*/
$filename = end(explode("/", __FILE__));
include 'config_db.php';

if (isset($_POST["email"]) && isset($_POST["auth"]) && isset($_POST["deviceID"]) && isset($_POST["sensorID"]) && isset($_POST["nickname"]) && isset($_POST["type"]) && isset($_POST["brand"]) && isset($_POST["model"])) {
	$dbhandle = connectToDB();
	$userID = $dbhandle->query("SELECT id FROM user WHERE email='".$_POST["email"]."' AND auth='".$_POST["auth"]."'");
	$userID = $userID->fetch_array(MYSQLI_ASSOC);
	if (empty($userID)) {
		$dbhandle->close();
		echo "ERROR: User authorization failed.";
	} else {
		$deviceID = $dbhandle->query("SELECT id FROM device WHERE type='".$_POST["type"]."' AND brand='".$_POST["brand"]."' AND model='".$_POST["model"]."'");
		$deviceID = $deviceID->fetch_array(MYSQLI_ASSOC);
		if (empty($deviceID)) {
			$dbhandle->close();
			echo "ERROR: Get device ID failed.";
		} else {
			$pluginID = $dbhandle->query("SELECT id FROM plugin WHERE board_device_id='".$_POST["deviceID"]."' AND sensor_id='".$_POST["sensorID"]."'");
			$pluginID = $pluginID->fetch_array(MYSQLI_ASSOC);
			if (empty($pluginID)) {
				$dbhandle->close();
				echo "ERROR: Get plugin ID failed.";
			} else {
				if ($dbhandle->query("INSERT INTO user_has_plugin VALUES (".$userID["id"].", ".$pluginID["id"].", ".$deviceID["id"].", '".$_POST["nickname"]."')"))
					echo "Successfully Saved.";
				else
					echo "ERROR: Failed to insert data to database.";
				$dbhandle->close();
			}
		}
	}
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
