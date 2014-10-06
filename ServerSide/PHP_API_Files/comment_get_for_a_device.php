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
	$result = $dbhandle->query("SELECT email FROM user WHERE email='".$_POST["email"]."' AND auth='".$_POST["auth"]."'");
	$result = $result->fetch_array(MYSQLI_ASSOC);
	if (empty($result) || count($result) != 1) {
		$dbhandle->close();
		echo "ERROR: Authorization failed.";
	} else {
		$rows = array();
		$result = $dbhandle->query("SELECT user.email, comment.content FROM user, comment, device WHERE user.email!='".$_POST["email"]."' AND user.id=comment.user_id AND device.id=comment.device_id AND device.type='".$_POST["type"]."' AND device.brand='".$_POST["brand"]."' AND device.model='".$_POST["model"]."'");
		while($row = $result->fetch_array(MYSQLI_ASSOC))
			$rows[] = $row;
		$dbhandle->close();
		$rowLength = count($rows);
		$ans = '{"result":[';
		for ($i = 0; $i < $rowLength; $i++) {
			$ans .= '{"user_email":"'.$rows[$i]['email'].'", "content":"'.$rows[$i]['content'].'"}';
			if ($i != $rowLength-1)
				$ans .= ',';
		}
		$ans .= ']}';
		echo $ans;
	}
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
