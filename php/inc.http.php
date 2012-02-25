<?php
Class http {
	public $cookie=array();
	
	public function GetCookie() {
		return $this->cookie;
	}
	
	public function SetCookie($cookie) {
		$this->cookie=$cookie;
	}
	
	public function GetHTML($host, $url,$port=80) {
		$address=gethostbyname($host);
		if($port==443) $address="ssl://$address";
		$packet = "GET $url HTTP/1.0\r\n";
		$packet .= "Host: $host\r\n";
		if(count($this->cookie)>0){
			$packet .= "Cookie: ";
			for($i=0;$i<count($this->cookie);$i++)
				$packet .= $this->cookie[$i].";";
			$packet=substr($packet, 0, -1);
			$packet .="\r\n";
		}
		$packet .= "Connection: Close\r\n\r\n";
		$sock=fsockopen($address,$port);
		if (!$sock) {
			die("FAIL: No response from host");
		}
		fputs($sock,$packet);
		$html="";
		while (!feof($sock)) {
			$html.=fgets($sock);
		}
		fclose($sock);
		$this->ExtractCookie($html);
		return $html;
	}
	public function parse_asp($parse_asp){
		$data="";
		foreach(array("__VIEWSTATE","__EVENTVALIDATION") as $name)
			if(ereg('id="'.$name.'" value="([^"]+)',$parse_asp,$match))
				$data.="&$name=".urlencode($match[1]);
		return $data;
	}
	public function PostHTML($host, $url, $data,$port=80) {
		$address=gethostbyname($host);
		if($port==443) $address="ssl://$address";
		$packet="POST $url HTTP/1.1\r\n";
		$packet.="Host: $host\r\n";
		if(count($this->cookie)>0){
			$packet .= "Cookie: ";
			for($i=0;$i<count($this->cookie);$i++)
				$packet .= $this->cookie[$i].";";
			$packet=substr($packet, 0, -1);
			$packet .="\r\n";
		}
		$packet.="Content-Type: application/x-www-form-urlencoded\r\n";
		$packet.="Content-Length: ".strlen($data)."\r\n";
		$packet.="Connection: Close\r\n\r\n";
		$packet.=$data;
		$sock=fsockopen($address,$port);
		if (!$sock) {
			die("FAIL: No response from host");
		}
		fputs($sock,$packet);
		$html="";
		while (!feof($sock)) {
			$html.=fgets($sock);
		}
		fclose($sock);
		$this->ExtractCookie($html);
		return $html;
	}
	
	public function ExtractCookie($response) {
		if(preg_match_all("`Set-Cookie: (.*)`i", $response, $match))
			foreach($match[1] as $cookie){
				$cookie=explode(";", $cookie);
				array_push($this->cookie,$cookie[0]);
			}
	}
	
}
?>