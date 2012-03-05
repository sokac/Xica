<?php
include('aspparser.php');

Class Xica {
    public $korisnik = '';
    public $stanje = 0;
    public $razinaPrava = 0;
    public $slikaURL = '';
    
    public $racuni = array();
    public $danasPotroseno = 0.0;
    public $ukupnoPotroseno = 0.0;
    
    private $prava = array('0'=>0, '1'=> 49.2, '2' => 98.4, '2.5' => 123.0, '3' => 147.6);
    
    private $aspParser;
    public function __construct() {
        $this->aspParser = new ASPParser();
    }
    public function login($user, $pass, &$errorMsg = '') {
        $html = $this->aspParser->getData(ASPParser::METHOD_GET, 'http://www.cap.srce.hr/login.ashx');
        if(preg_match('/name="AuthState" value="([^"]+)/', $html, $match)) {
            $data['AuthState'] = str_replace('&amp;', '&', $match[1]);
            $data['username'] = $user;
            $data['password'] = $pass;
            $data['Submit'] = 'Prijavi se';
            $html = $this->aspParser->getData(ASPParser::METHOD_POST, 'https://login.aaiedu.hr/sso/module.php/core/loginuserpass.php?', $data);
            if(preg_match('/name="SAMLResponse" value="([^"]+)/', $html, $match)) {


                $html = $this->aspParser->getData(ASPParser::METHOD_POST, 'http://www.cap.srce.hr/login.ashx', array('SAMLResponse' => $match[1]));
                if(preg_match('%<span id="rightframe_ImePrezimeLabel"><b><font face="Verdana" size="3">([^<]+?)</font></b></span>%', $html, $match))
                    $this->korisnik = $match[1];
                else {
                    $errorMsg = 'Greska prilikom ocitavanja imena';
                    return false;
                }

                if(preg_match('%<span id="rightframe_PreostalaSubvencijaLabel"><font face="Verdana" size="2">([^<]+?)</font></span>%', $html, $match))
                    $this->stanje = (float) str_replace(',', '.', $match[1]);
                else {
                    $errorMsg = 'Greska prilikom ocitavanja stanja';
                    return false;
                }
                if(preg_match('%<span id="rightframe_RazinaPravaLabel"><b><font face="Verdana" size="2">([^ ]+?) kompletna obroka dnevno</font></b></span>%', $html, $match))
                    $this->razinaPrava = $match[1];
                else {
                    $errorMsg = 'Greska prilikom ocitavanja prava';
                    return false;
                }

                if(preg_match('%<img id="rightframe_Image1" src="TempPic\\\\([0-9]+?).jpg" height="150" width="150" />%', $html, $match))
                    $this->slikaURL = "/TempPic\\{$match[1]}.jpg";
                else {
                    $errorMsg = 'Greska prilikom ocitavanja slike';
                    return false;
                }
            } else {
                $errorMsg = 'Greska x02';
                return false;
            }
        } else {
            $errorMsg = 'Greska x01';
            return false;
        }
        return true;
    }

    public function pregledRacuna(&$errorMsg = '') {
        if(!empty($this->racuni))
            return $this->racuni;
                    
        $html = $this->aspParser->getData(ASPParser::METHOD_GET, 'http://www.cap.srce.hr/Dnevniknovo.aspx');
		$html=str_replace("\r\n","",$html);
		$pat='/__doPostBack[^>]+>([^<]+)<.a>[^<]+<\/td><td>([^<]+)<\/td><td>([^<]+)<\/td>[^>]*>([^<]+)/';

        $this->racuni = array();
		if(preg_match_all($pat,$html,$match)) {
			for($i=0;$i<count($match[0]);$i++) {
				$this->racuni[] = array(
                    'vrijeme' => $match[1][$i],
                    'restoran' => $match[2][$i],
                    'linija' => $match[3][$i],
                    'iznos' => $match[4][$i]);

                $date = new DateTime($this->racuni[$i]["vrijeme"]);

                if(date_format($date,"d")==date("d"))
					$this->danasPotroseno += (float) str_replace(",", ".",$this->racuni[$i]["iznos"]);
			}
		}

        if(preg_match('%<span id="rightframe_Label2">([0-9,]+)</span>%', $html, $match)) {
            $this->ukupnoPotroseno = (float)str_replace(',', '.', $match[1]);
        } else {
            $errorMsg = 'Greska prilikom ocitavanja ukupne potrosnje';
            return false;
        }

        return $this->racuni;
    }

    public function racunInfo($racunBroj) {
		if($racunBroj < 9)
			$racunBroj = '0'.($racunBroj + 1);
		else
			$racunBroj = $racunBroj + 1;

        $html = $this->aspParser->getData(ASPParser::METHOD_POST, "http://www.cap.srce.hr/Dnevniknovo.aspx", array("__EVENTTARGET" => 'ctl00$rightframe$DataGrid1$ctl'.$racunBroj.'$lb'));

		if(preg_match('%<span id="rightframe_Label5"><font face="Verdana" size="2">([^<]+)</font></span>%', $html, $match))
			$ret['opis']=$match[1];
		if(preg_match('%<span id="rightframe_Label1"><font face="Verdana" size="2">([^<]+)</font></span>%', $html, $match))
			$ret['vrijeme']=$match[1];
		if(preg_match('%<span id="rightframe_Label2" class="smalltextcell">([^<]+)</span>%', $html, $match))
			$ret['iznos']=$match[1];
        if(preg_match('%<span id="rightframe_Label4" class="smalltextcell">([^<]+)</span>%', $html, $match))
			$ret['participacija']=$match[1];

		$ret['artikli']=array();

		$pat='%<td>([^<]+)</td><td align="center">([^<]+)</td><td align="right">[^<]+</td><td align="right">([^<]+)</td><td align="right">([^<]+)</td>%';
		if(preg_match_all($pat, $html, $match)) {
			for($i = 0; $i < count($match[0]); $i++) {
				$ret['artikli'][$i]['naziv'] = $match[1][$i];
				$ret['artikli'][$i]['komada'] = $match[2][$i];
				$ret['artikli'][$i]['ukupno'] = (float)str_replace(',', '.', $match[3][$i]);
				$ret['artikli'][$i]['subvencija'] = (float)str_replace(',', '.', $match[4][$i]);
			}
		}
        return $ret;
    }

    public function toJson() {
        return json_encode(array(
            'korisnik' => $this->korisnik,
            'stanje' => $this->stanje,
            'razina' => $this->razinaPrava,
            'slika' => $this->slikaURL,
            'racuni' => $this->racuni,
            'ostalo' => $this->prava[$this->razinaPrava]-$this->danasPotroseno,
            'potroseno_sada' => $this->ukupnoPotroseno,
            'potroseno_kraj_mj' => round($this->ukupnoPotroseno + ($this->ukupnoPotroseno / ((int) date("d"))) * ((int)date("t")-(int)date("d")), 2)
        ));
    }
}

$xica = new Xica();
if($xica->login($_REQUEST['username'], $_REQUEST['password'], $error) == false) {
	die('FAIL:' . $error);
}

$xica->pregledRacuna();

if(isset($_GET['racun']))
    echo json_encode($xica->racunInfo((int) $_GET['racun']));
else
    echo $xica->toJson();
