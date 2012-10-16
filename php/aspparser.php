<?php
Class ASPParser {
    private $cookiejar;
    private $data = array();
    const METHOD_POST = 0;
    const METHOD_GET = 1;

    public function __construct() {
        $this->cookiejar = tempnam('/tmp', 'xica');
    }

    public function getData($method, $url, $data=array()) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
        curl_setopt($ch, CURLOPT_COOKIEFILE, $this->cookiejar);
        curl_setopt($ch, CURLOPT_COOKIEJAR, $this->cookiejar);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

        if($method == self::METHOD_POST) {
            $data = array_merge($this->data, $data);
            $data_string = '';
            foreach ($data as $k=>$v)
                $data_string .= $k . '=' . urlencode($v) . '&';
            $data_string = substr($data_string, 0, -1);

            curl_setopt($ch, CURLOPT_POST, count($data));
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data_string);

        }
        
        $this->parse_asp($response = curl_exec($ch));
        curl_close($ch);
        return $response;
    }

    private function parse_asp($parse_asp){
		$this->data = array();
		foreach(array("__VIEWSTATE","__EVENTVALIDATION") as $name)
			if(preg_match('/id="'.$name.'" value="([^"]+)/', $parse_asp, $match))
				$this->data[$name] = $match[1];
	}
}