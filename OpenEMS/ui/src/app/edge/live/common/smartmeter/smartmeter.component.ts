import { Component } from '@angular/core';
import { AbstractFlatWidget } from 'src/app/shared/genericComponents/flat/abstract-flat-widget';
import { ChannelAddress, CurrentData, Utils } from 'src/app/shared/shared';

@Component({
  selector: 'smartmeter',
  templateUrl: './smartmeter.component.html',
})
export class SmartmeterComponent extends AbstractFlatWidget {
  private static readonly ID: ChannelAddress = new ChannelAddress(
    'smartmeter0',
    'Id'
  );
  private static readonly ENERGY_DELIVERED_TARRIF_1: ChannelAddress =
    new ChannelAddress('smartmeter0', 'EnergyDeliveredTarrif1');
  private static readonly ENERGY_DELIVERED_TARRIF_2: ChannelAddress =
    new ChannelAddress('smartmeter0', 'EnergyDeliveredTarrif2');
  private static readonly ENERGY_RECEIVED_TARRIF_1: ChannelAddress =
    new ChannelAddress('smartmeter0', 'EnergyReceivedTarrif1');
  private static readonly ENERGY_RECEIVED_TARRIF_2: ChannelAddress =
    new ChannelAddress('smartmeter0', 'EnergyReceivedTarrif2');
  private static readonly TARIFF_INDICATOR: ChannelAddress = new ChannelAddress(
    'smartmeter0',
    'TariffIndicator'
  );
  private static readonly ACTUAL_POWER_DELIVERED: ChannelAddress =
    new ChannelAddress('smartmeter0', 'ActualPowerDelivered');
  private static readonly ACTUAL_POWER_RECEIVED: ChannelAddress =
    new ChannelAddress('smartmeter0', 'ActualPowerReceived');
  private static readonly GAS_DELIVERED: ChannelAddress = new ChannelAddress(
    'smartmeter0',
    'GasDelivered'
  );
  private static readonly ENERGY_DELIVERED: ChannelAddress = new ChannelAddress(
    'smartmeter0',
    'EnergyDelivered'
  );
  private static readonly ENERGY_RECEIVED: ChannelAddress = new ChannelAddress(
    'smartmeter0',
    'EnergyReceived'
  );

  private static readonly TIMESTAMP: ChannelAddress = new ChannelAddress(
    'smartmeter0',
    'Timestamp'
  );

  public readonly CONVERT_TO_CELCIUS = Utils.CONVERT_TO_CELCIUS;
  public readonly CONVERT_TO_VOLT = Utils.CONVERT_TO_VOLT;
  public readonly CONVERT_TO_KILO_WATTHOURS = Utils.CONVERT_TO_KILO_WATTHOURS;

  id: any;
  EnergyDeliveredTarrif1: any;
  EnergyDeliveredTarrif2: any;
  EnergyReceivedTarrif1: any;
  EnergyReceivedTarrif2: any;
  TariffIndicator: any;
  ActualPowerDelivered: any;
  ActualPowerReceived: any;
  GasDelivered: any;
  EnergyDelivered: any;
  EnergyReceived: any;
  Timestamp: any;

  protected getChannelAddresses(): ChannelAddress[] {
    let channelAddresses: ChannelAddress[] = [
      SmartmeterComponent.ID,
      SmartmeterComponent.ENERGY_DELIVERED_TARRIF_1,
      SmartmeterComponent.ENERGY_DELIVERED_TARRIF_2,
      SmartmeterComponent.ENERGY_RECEIVED_TARRIF_1,
      SmartmeterComponent.ENERGY_RECEIVED_TARRIF_2,
      SmartmeterComponent.TARIFF_INDICATOR,
      SmartmeterComponent.ACTUAL_POWER_DELIVERED,
      SmartmeterComponent.ACTUAL_POWER_RECEIVED,
      SmartmeterComponent.GAS_DELIVERED,
      SmartmeterComponent.ENERGY_DELIVERED,
      SmartmeterComponent.ENERGY_RECEIVED,
      SmartmeterComponent.TIMESTAMP,
    ];
    return channelAddresses;
  }
  protected onCurrentData(currentData: CurrentData) {
    this.id = currentData.allComponents[SmartmeterComponent.ID.toString()];
    this.EnergyDeliveredTarrif1 =
      currentData.allComponents[
        SmartmeterComponent.ENERGY_DELIVERED_TARRIF_1.toString()
      ];
    this.EnergyDeliveredTarrif2 =
      currentData.allComponents[
        SmartmeterComponent.ENERGY_DELIVERED_TARRIF_2.toString()
      ];
    this.EnergyReceivedTarrif1 =
      currentData.allComponents[
        SmartmeterComponent.ENERGY_RECEIVED_TARRIF_1.toString()
      ];
    this.EnergyReceivedTarrif2 =
      currentData.allComponents[
        SmartmeterComponent.ENERGY_RECEIVED_TARRIF_2.toString()
      ];
    this.TariffIndicator =
      currentData.allComponents[
        SmartmeterComponent.TARIFF_INDICATOR.toString()
      ];
    this.ActualPowerDelivered =
      currentData.allComponents[
        SmartmeterComponent.ACTUAL_POWER_DELIVERED.toString()
      ];
    this.ActualPowerReceived =
      currentData.allComponents[
        SmartmeterComponent.ACTUAL_POWER_RECEIVED.toString()
      ];
    this.GasDelivered =
      currentData.allComponents[SmartmeterComponent.GAS_DELIVERED.toString()];
    this.EnergyDelivered =
      currentData.allComponents[
        SmartmeterComponent.ENERGY_DELIVERED.toString()
      ];
    this.EnergyReceived =
      currentData.allComponents[SmartmeterComponent.ENERGY_RECEIVED.toString()];
    this.Timestamp =
      currentData.allComponents[SmartmeterComponent.TIMESTAMP.toString()];
  }
}

//   // async presentModal() {
//   //   const modal = await this.modalController.create({
//   //     component: GridModalComponent,
//   //     componentProps: {
//   //       edge: this.edge,
//   //     },
//   //   });
//   //   return await modal.present();
//   // }
// }
