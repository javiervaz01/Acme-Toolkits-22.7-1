package acme.features.administrator.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdministratorDashboardItem {

	public String currency;
	
	public Double average;
	
	public Double deviation;
	
	public Double min;
	
	public Double max;
	
}