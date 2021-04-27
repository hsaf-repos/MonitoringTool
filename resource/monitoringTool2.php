<?php
$url = 'C:\Users\telespazio\Documents\HSAF\eclipse-workspace\MonitoringTool\resource\lastUpdate.h01';
$dati = str_getcsv(file_get_contents($url), " ");

?>

<!DOCTYPE html>
<html>
<link href="style.css" rel="stylesheet" type="text/css">



<body>
	<header>

		<meta http-equiv="refresh" content="10">
		<div id="header">
			<p align=center>
				<img
					src="C:\Users\telespazio\Documents\HSAF\eclipse-workspace\MonitoringTool\resource\Hsaf_Logo.JPG"
					width="25%" align="center" />
			</p>
		</div>
	</header>
	<main>

		<table>



			<tr>
				<th>Product ID</th>
				<th>Generation Rate Status</th>
				<th>Size Status</th>
				<th>Daily Generation Status</th>
				<th>Log File</th>
				<th>Reset Log</th>
			</tr>
			<tr>
				<td>H01</td>
				<td data-status="NOK" class="status">NOK</td>
				<td data-status="OK" class="status">OK</td>
				<td data-status="OK" class="status">OK</td>
				<td><a
					href="C:\Users\telespazio\Documents\HSAF\eclipse-workspace\MonitoringTool\log\h01.log">C:\Users\Telespazio\eclipse-workspace\MonitoringTool\log\h01.log</a></td>
				<td><button
						href="?delete=C:\Users\telespazio\Documents\HSAF\eclipse-workspace\MonitoringTool\log\h01.log">Reset</button></td>
			</tr>
			<tr>
				<td>H02B</td>
				<td data-status="NOK" class="status">NOK</td>
				<td data-status="OK" class="status">OK</td>
				<td data-status="NOK" class="status">NOK</td>
				<td><a
					href="C:\Users\telespazio\Documents\HSAF\eclipse-workspace\MonitoringTool\log\h02B.log">C:\Users\Telespazio\eclipse-workspace\MonitoringTool\log\h02B.log</a></td>
				<td><button
						href="?delete=C:\Users\telespazio\Documents\HSAF\eclipse-workspace\MonitoringTool\log\h02B.log">Reset</button></td>
			</tr>

		</table>
	</main>
	<footer>

		<div id="footer">
			<p>
				NOW: <span id="datetime"></span>
			</p>
			
			LAST UPDATE: 24.11.2020 15:38:15
			
			



		</div>
	</footer>

</body>
</html>




<script>
var dt = new Date();
document.getElementById("datetime").innerHTML = (("0"+dt.getDate()).slice(-2)) +"."+ (("0"+(dt.getMonth()+1)).slice(-2)) +"."+ (dt.getFullYear()) +" "+ (("0"+dt.getHours()).slice(-2)) +":"+ (("0"+dt.getMinutes()).slice(-2))+ ":" + (("0"+dt.getSeconds()).slice(-2));
</script>
