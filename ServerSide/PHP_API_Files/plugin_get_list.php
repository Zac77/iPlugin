<?php
/*
 * Description: This file is used to get a list for all plugins which a user has.
 * Author: Zac (Qi ZHANG)
 * Create Date: 10/02/2014
*/
$filename = end(explode("/", __FILE__));
include 'config_db.php';

if (isset($_POST["email"]) && isset($_POST["auth"])) {
	$rows = array();
	$dbhandle = connectToDB();
	$result = $dbhandle->query("SELECT plugin.board_device_id, plugin.sensor_id, user_has_plugin.nickname, device.type, device.brand, device.model, device.consume FROM user, user_has_plugin, plugin, device WHERE user.email='".$_POST["email"]."' AND user.auth='".$_POST["auth"]."' AND user.id=user_has_plugin.user_id AND user_has_plugin.plugin_id=plugin.id AND user_has_plugin.device_id=device.id");
	while($row = $result->fetch_array(MYSQLI_ASSOC))
		$rows[] = $row;
	$dbhandle->close();
	$rowLength = count($rows);
	$ans = '{"result":[';
	for ($i = 0; $i < $rowLength; $i++) {
		$ans .= '{"board_device_id":"'.$rows[$i]['board_device_id'].'", "sensor_id":"'.$rows[$i]['sensor_id'].'", "nickname":"'.$rows[$i]['nickname'].'", "device_type":"'.$rows[$i]['type'].'", "brand":"'.$rows[$i]['brand'].'", "model":"'.$rows[$i]['model'].'", "consume":"'.$rows[$i]['consume'].'"}';
		if ($i != $rowLength-1)
			$ans .= ',';
	}
	$ans .= ']}';
	echo $ans;
} else
	echo "ERROR: ".$filename." didn't get correct parameters.";

?>
