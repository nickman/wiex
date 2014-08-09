import java.text.*;

public String[][] postProcess(String[][] input) {
	DecimalFormat df = new DecimalFormat("###,###,###");	
	int rowCnt = 0;	
	input.each() { row ->
		int colCnt = 0;
		row.each() { column ->
			if(!column.getClass().getName().equals("java.lang.String")) {				
				input[rowCnt][colCnt] = "&nbsp;";				
			} else {
				try {
					Number number = df.parse(column);
					input[rowCnt][colCnt] = df.format(number.doubleValue());
				} catch (Exception e) {}
			}
			colCnt++;
		}
		rowCnt++;
	}
	//Thread.currentThread().sleep(200);
	return input;
}