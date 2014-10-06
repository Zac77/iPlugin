<?php
/*
 * Description: This file is used to get comments for a given device.
 * Author: Zac (Qi ZHANG)
 * Create Date: 10/02/2014
*/
$filename = end(explode("/", __FILE__));
include 'config_db.php';

if (isset($_POST["email"]) && isset($_POST["auth"]) && isset($_POST["type"]) && isset($_POST["brand"]) && isset($_POST["model"])) {
	$dbhandle = connectToDB();
	$result = $dbhandle->query("SELECT comment.content FROM user, comment, device WHERE user.email='".$_POST["email"]."' AND user.auth='".$_POST["auth"]."' AND user.id=comment.user_id AND comment.device_id=device.id AND device.type='".$_POST["type"]."' AND device.brand='".$_POST["brand"]."' AND device.model='".$_POST["model"]."'");
	$result = $result->fetch_array(MYSQLI_ASSOC);
	$dbhandle->close();
	if (empty($result) || count($result) != 1)
		echo "";
	else
		echo $result["content"];
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
