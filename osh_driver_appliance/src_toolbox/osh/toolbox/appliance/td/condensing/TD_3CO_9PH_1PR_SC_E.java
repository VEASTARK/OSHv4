package osh.toolbox.appliance.td.condensing;

import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationExtra;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.toolbox.appliance.ToolApplianceConfigurationProgram;

/**
 * Dryer Low Energy 3 Configurations 9 Phase Multi Commodity
 * 
 * @author Ingo Mauser
 *
 */
public class TD_3CO_9PH_1PR_SC_E {

	public static ToolApplianceConfiguration[] configurations = {
			new ToolApplianceConfiguration(
					0,
					new ToolApplianceConfigurationProgram(0, "Drying 0",
							"Drying 0", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"pure electric uninterruptible",
									new String[] { "Drying 0", "Drying 1",
											"Drying 2", "Drying 3", "Drying 4",
											"Drying 5", "Drying 6", "Drying 7",
											"Drying 8" },
									new String[] {
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_0_A_0.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_0_A_1.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_0_A_2.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_0_A_3.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv", },
									0, 1, -1, -1),
							}),
			new ToolApplianceConfiguration(
					1,
					new ToolApplianceConfigurationProgram(0, "Drying 1",
							"Drying 1", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"pure electric uninterruptible",
									new String[] { "Drying 0", "Drying 1",
											"Drying 2", "Drying 3", "Drying 4",
											"Drying 5", "Drying 6", "Drying 7",
											"Drying 8" },
									new String[] {
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_1_A_0.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_1_A_1.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_1_A_2.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_1_A_3.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv", },
									0, 1, -1, -1),
							}),
			new ToolApplianceConfiguration(
					2,
					new ToolApplianceConfigurationProgram(0, "Drying 2",
							"Drying 2", "wasssssccchn"),
					new ToolApplianceConfigurationExtra[] {},
					new ToolApplianceConfigurationProfile[] {
							new ToolApplianceConfigurationProfile(
									0,
									"pure electric uninterruptible",
									new String[] { "Drying 0", "Drying 1",
											"Drying 2", "Drying 3", "Drying 4",
											"Drying 5", "Drying 6", "Drying 7",
											"Drying 8" },
									new String[] {
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_2_A_0.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_2_A_1.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_2_A_2.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv",
											"data/profiles/renamed/td/TD_CO_2_A_3.csv",
											"data/profiles/renamed/td/TD_CO_pause2.csv", },
									0, 1, -1, -1),

							}) };

}
