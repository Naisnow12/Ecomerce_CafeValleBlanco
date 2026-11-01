import { Component } from '@angular/core';
import { Header } from '../../../core/header/header';
import { Footer } from '../../../core/footer/footer';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [Header, Footer, RouterOutlet],
  selector: 'app-layout-client',
  templateUrl: './layout-client.html',
})
export class LayoutClient { }
