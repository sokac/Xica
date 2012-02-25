<?php
	include("inc.http.php");
	$http = new http();
	$ret=array();
	$prava = array("0"=>0, "1"=> 49.2, "2" => 98.4, "2.5" => 123.0, "3" => 147.6);
	
	$jmbag=$_REQUEST["jmbag"]; // obavezno
	$jmbg=$_REQUEST["jmbg"];   // obavezno
	$racun=$_REQUEST["racun"]; // id racuna iz liste, pocevsi od 1
	
	$data="__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwULLTEwOTc4NDMyMjRkZEh%2FmRix953PbZj%2BTtS0h1oQWFd5&__EVENTVALIDATION=%2FwEWBALQip6yBAKZmN7rBAKcmN7rBALJjIyiAjzRaQ99%2F1TXylvhlDnEX%2FmFDeRw&ctl00%24rightframe%24Textbox2=".$jmbag."&ctl00%24rightframe%24Textbox1=".$jmbg."&ctl00%24rightframe%24Button1=Prijava";
	
	$cache="";
	$cookie="";
	$asp_data="";
	if(file_exists("cache/$jmbag.txt") && file_exists("cache/$jmbag.cookie.txt") && file_exists("cache/$jmbag.aspdata.txt") && filemtime("cache/$jmbag.txt")+300>time()){
		@$cache=file_get_contents("cache/$jmbag.txt");
		@$cookie=file_get_contents("cache/$jmbag.cookie.txt");
		@$asp_data=file_get_contents("cache/$jmbag.aspdata.txt");
	}
	if($cache==""){
		$html=$http->PostHTML("www.cap.srce.hr","/Prijava.aspx",$data);
		if(!ereg('<span id="ctl00_rightframe_Label1"><b>([^<]+)</b></span>',$html,$match))
			die("FAIL: Krivi login podaci!");
		$ret["korisnik"]=$match[1];
		
		if(!ereg('<span id="ctl00_rightframe_Label4">([^<]*)</span>',$html,$match))
			die("FAIL: Greska prilikom ocitavanja racuna!");
		$ret["stanje"]=(float)str_replace(",", ".",$match[1]);
		
		if(!ereg('<span id="ctl00_rightframe_Label3">([0-9.]+)',$html,$match))
			die("FAIL: Greska prilikom ucitavanja razine prava");
		$ret["razina"]=$match[1];
		
		if(ereg('<img id="ctl00_rightframe_Image1" src="TempPic.([0-9]*).jpg"',$html,$match)) {
			$url="/TempPic\\".$match[1].".jpg";
			$ret["slika"]=$url;
		}
		
		
		$html=$http->PostHTML("www.cap.srce.hr","/Student.aspx","__EVENTTARGET=ctl00%24rightframe%24LinkButton2".$http->parse_asp($html));
		
		$spent_total = 0.0;
		if(ereg("<span id=\"ctl00_rightframe_Label3\">([0-9,]+)</span>",$html,$match)) {
			$spent_total = str_replace(",", ".",$match[1]);
		}
		$ret["racuni"]=array();
		$spent = 0.0;
		
		
		$html=str_replace("\r\n","",$html);
		$pat='/__doPostBack[^>]+>([^<]+)<.a>[^<]+<\/td><td>([^<]+)<\/td><td>([^<]+)<\/td>[^>]*>([^<]+)/';
		if(preg_match_all($pat,$html,$match)) {
			for($i=0;$i<count($match[0]);$i++) {
				$ret["racuni"][$i]["vrijeme"]=$match[1][$i];
				$ret["racuni"][$i]["restoran"]=$match[2][$i];
				$ret["racuni"][$i]["linija"]=$match[3][$i];
				$ret["racuni"][$i]["iznos"]=$match[4][$i];
				
				$date = new DateTime($ret["racuni"][$i]["vrijeme"]);
				if(date_format($date,"d")==date("d"))
					$spent += (float) str_replace(",", ".",$ret["racuni"][$i]["iznos"]);
			}
		}
		$ret["ostalo"] = $prava[$ret["razina"]]-$spent;
		
		$ret["potroseno_sada"] = $spent_total;
		$ret["potroseno_kraj_mj"] = round($spent_total + ($spent_total / ((int) date("d"))) * ((int)date("t")-(int)date("d")), 2);
		
		$asp_data=$http->parse_asp($html);
		// save 3 file
		file_put_contents("cache/$jmbag.txt",json_encode($ret));
		file_put_contents("cache/$jmbag.aspdata.txt",$http->parse_asp($html));
		file_put_contents("cache/$jmbag.cookie.txt", implode(";", $http->GetCookie()));
	}else{
		$ret=json_decode($cache, true);
		$http->SetCookie(explode(";", $cookie));
	}
	
	if($racun!=""){
		if($racun<9)
			$target="0".($racun+1);
		else
			$target=$racun+1;
		$html=$http->PostHTML("www.cap.srce.hr","/Dnevniknovo.aspx","__EVENTTARGET="."ctl00%24rightframe%24DataGrid1%24ctl".$target."%24lb".$asp_data);
		$ret=array();
		if(ereg('<span id="ctl00_rightframe_Label5">([^<]+)</span>',$html,$match))
			$ret["opis"]=$match[1];
		if(ereg('<span id="ctl00_rightframe_Label1">([^<]+)</span>',$html,$match))
			$ret["vrijeme"]=$match[1];
		if(ereg('<span id="ctl00_rightframe_Label2">([^<]+)</span>',$html,$match))
			$ret["iznos"]=$match[1];
		if(ereg('<span id="ctl00_rightframe_Label4">([^<]+)</span>',$html,$match))
			$ret["participacija"]=$match[1];
		$ret["artikli"]=array();
		$pat='/<td>([^<]+)<\/td><td align="center">([^<]+)<\/td><td align="right">[^<]+<\/td><td align="right">([^<]+)<\/td><td align="right">([^<]+)<\/td>/';
		if(preg_match_all($pat,$html,$match)) {
			for($i=0;$i<count($match[0]);$i++) {
				$ret["artikli"][$i]["naziv"]=$match[1][$i];
				$ret["artikli"][$i]["komada"]=$match[2][$i];
				$ret["artikli"][$i]["ukupno"]=$match[3][$i];
				$ret["artikli"][$i]["subvencija"]=$match[4][$i];
			}
		}
	}
	echo json_encode($ret);
?>