import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LFGMainComponent } from './lfgmain.component';

describe('LFGMainComponent', () => {
  let component: LFGMainComponent;
  let fixture: ComponentFixture<LFGMainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LFGMainComponent ],
      imports: [RouterTestingModule]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LFGMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
