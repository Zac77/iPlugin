<?php
/*
 * Description: This file is used to authorize an user's info.
 * Author: Zac (Qi ZHANG)
 * Create Date: 09/26/2014
*/
$filename = end(explode("/", __FILE__));
include 'config_db.php';

if (isset($_POST["email"]) && isset($_POST["auth"])) {
	$dbhandle = connectToDB();
	$result = $dbhandle->query("SELECT email FROM user WHERE email='".$_POST["email"]."' AND auth='".$_POST["auth"]."'");
	$result = $result->fetch_array(MYSQLI_ASSOC);
	$dbhandle->close();
	if (empty($result) || count($result) != 1)
		echo "f";
	else
		echo "s";
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
