<?php
/*
 * Description: This file is used to add/edit a comment.
 * Author: Zac (Qi ZHANG)
 * Create Date: 10/02/2014
*/
$filename = end(explode("/", __FILE__));
include 'config_db.php';

if (isset($_POST["email"]) && isset($_POST["auth"]) && isset($_POST["type"]) && isset($_POST["brand"]) && isset($_POST["model"])) {
	$dbhandle = connectToDB();
	$result = $dbhandle->query("SELECT user.id, device.id FROM user, comment, device WHERE user.email='".$_POST["email"]."' AND user.auth='".$_POST["auth"]."' AND user.id=comment.user_id AND comment.device_id=device.id AND device.type='".$_POST["type"]."' AND device.brand='".$_POST["brand"]."' AND device.model='".$_POST["model"]."'");
	$result = $result->fetch_array(MYSQLI_NUM);
	$secondResult = true;
	if (!empty($result)) {
		$secondResult = $secondResult && ($dbhandle->query("DELETE FROM comment WHERE user_id=".$result[0]." AND device_id=".$result[1]));
		if (isset($_POST["content"]))
			$secondResult = $secondResult && ($dbhandle->query("INSERT INTO comment VALUES (".$result[0].", ".$result[1].", '".$_POST["content"]."')"));
	} else if (isset($_POST["content"]))
		$secondResult = $secondResult && ($dbhandle->query("INSERT INTO comment VALUES ((SELECT id FROM user WHERE user.email='".$_POST["email"]."' AND user.auth='".$_POST["auth"]."'), (SELECT id FROM device WHERE device.type='".$_POST["type"]."' AND device.brand='".$_POST["brand"]."' AND device.model='".$_POST["model"]."'), '".$_POST["content"]."')"));
	$dbhandle->close();
	if ($secondResult)
			echo "Successfully Saved.";
		else
			echo "ERROR: Failed to insert new comment to the database.";
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
