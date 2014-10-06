<?php
/*
 * Description: This file is used to set the parameters for connecting to the database.
 * Author: Zac (Qi ZHANG)
 * Create Date: 09/26/2014
*/

// Configuration
$MySQL_IP = "";
$MySQL_Username = "";
$MySQL_Password = "";
$MySQL_Database_Name = "";

// Functions
function connectToDB() {
	global $MySQL_IP, $MySQL_Username, $MySQL_Password, $MySQL_Database_Name;
	$dbhandle = new mysqli($MySQL_IP, $MySQL_Username, $MySQL_Password, $MySQL_Database_Name);
	if ($dbhandle->connect_errno) {
		echo "ERROR: Failed to connect to MySQL: (" . $dbhandle->connect_errno . ") " . $dbhandle->connect_error;
		exit();
	}
	return $dbhandle;
}

?>
