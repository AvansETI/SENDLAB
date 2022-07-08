import { Component } from '@angular/core';
import { AbstractFlatWidget } from 'src/app/shared/genericComponents/flat/abstract-flat-widget';
import { ChannelAddress, CurrentData, Utils } from 'src/app/shared/shared';

@Component({
  selector: 'chargingstation',
  templateUrl: './chargingstation.component.html',
})
export class ChargingStationComponent extends AbstractFlatWidget {
  private static readonly GRID_L1_VOLTS: ChannelAddress = new ChannelAddress(
    'v2g0',
    'GridL1Volts'
  );
  private static readonly STATUS: ChannelAddress = new ChannelAddress(
    'v2g0',
    'Status'
  );
  private static readonly POWER_UNIT_TEMPERATURE: ChannelAddress =
    new ChannelAddress('v2g0', 'PowerUnitTemperature');

  public readonly CONVERT_TO_CELCIUS = Utils.CONVERT_TO_CELCIUS;
  public readonly CONVERT_TO_VOLT = Utils.CONVERT_TO_VOLT;

  status: any;
  temperature: any;
  statusText: String;
  gridvoltage: any;

  protected getChannelAddresses(): ChannelAddress[] {
    let channelAddresses: ChannelAddress[] = [
      ChargingStationComponent.GRID_L1_VOLTS,
      ChargingStationComponent.STATUS,
      ChargingStationComponent.POWER_UNIT_TEMPERATURE,
    ];
    return channelAddresses;
  }
  protected onCurrentData(currentData: CurrentData) {
    let gridvoltage =
      currentData.allComponents[
        ChargingStationComponent.GRID_L1_VOLTS.toString()
      ];
    this.gridvoltage = parseFloat(gridvoltage).toFixed(2);
    this.getStatus(
      currentData.allComponents[ChargingStationComponent.STATUS.toString()]
    );
    let temp =
      currentData.allComponents[
        ChargingStationComponent.POWER_UNIT_TEMPERATURE.toString()
      ];
    this.temperature = parseFloat(temp).toFixed(1);
  }

  protected getStatus(res) {
    switch (res) {
      case 0:
        this.statusText =
          'Connector not available, in error state or not installed';
        break;
      case 1:
        this.statusText = 'Connector idle / available';
        break;
      case 2:
        this.statusText = 'Connector plugged, initialising charge';
        break;
      case 3:
        this.statusText = 'Connector plugged, charging in progress';
        break;
      case 4:
        this.statusText = 'Connector plugged, dis-charging in progress';
        break;
      case 5:
        this.statusText = 'Connector plugged, charging finished or paused';
        break;
      default:
        this.statusText =
          'Connector not available, in error state or not installed';
        break;
    }
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
