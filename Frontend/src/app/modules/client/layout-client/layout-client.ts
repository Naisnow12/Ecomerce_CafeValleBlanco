import { Component } from '@angular/core';
import { Header } from '../../../core/header/header';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [Header, RouterOutlet],
  selector: 'app-layout-client',
  styles: ``,
  templateUrl: './layout-client.html',
})
export class LayoutClient { }
