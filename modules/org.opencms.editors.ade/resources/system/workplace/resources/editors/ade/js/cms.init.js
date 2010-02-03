

/**
 * Certain functions of ui.sortable need to be extended for better performance within OpenCms Advanced Direct Edit.<p>
 *
 * First of all the _uiHash function is replaced by a version that will grant access to the private plugin-functions and properties.
 * The other extended functions deal with special cases and scenarios of the Advanced Direct Edit use-case.<p>
 *
 */
$.extend($.ui.sortable.prototype, {
   /**
    * Returns the ui-object used with event-handlers.<p>
    *
    * @param {Object} inst
    */
   _uiHash: function(inst) {
      var self = inst || this;
      
      /* cms-addition:
       * return the sortable-plugin-object as self */
      return {
         helper: self.helper,
         placeholder: self.placeholder || $([]),
         position: self.position,
         originalPosition: self.originalPosition,
         offset: self.positionAbs,
         item: self.currentItem,
         sender: inst ? inst.element : null,
         self: self
      };
   },
   /**
    * Internal event-handler for the mouse-stop-event. Adjusted to show a reverting animation to the original position of the item
    * in case of a canceled sorting indicated by a hidden placeholder.<p>.
    *
    * @param {Object} event
    * @param {Object} noPropagation
    */
   _mouseStop: function(event, noPropagation) {
   
      if (!event) 
         return;
      
      //If we are using droppables, inform the manager about the drop
      if ($.ui.ddmanager && !this.options.dropBehaviour) 
         $.ui.ddmanager.drop(this, event);
      
      if (this.options.revert) {
         var self = this;
         var cur;
         
         /* cms-addition:
          * in case the placeholder is hidden use the initial offset for reverting */
         if (self.cmsStartOffset && self.placeholder.css('display') == 'none') {
            cur = self.cmsStartOffset;
         } else {
            cur = self.placeholder.offset();
         }
         
         self.reverting = true;
         
         $(this.helper).animate({
            left: cur.left - this.offset.parent.left - self.margins.left + (this.offsetParent[0] == document.body ? 0 : this.offsetParent[0].scrollLeft),
            top: cur.top - this.offset.parent.top - self.margins.top + (this.offsetParent[0] == document.body ? 0 : this.offsetParent[0].scrollTop)
         }, parseInt(this.options.revert, 10) || 500, function() {
            self._clear(event);
         });
      } else {
         this._clear(event, noPropagation);
      }
      
      return false;
      
   },
   /**
    * Check whether the pointer intersects with a certain item.<p>
    *
    * @param {Object} item
    */
   _intersectsWithPointer: function(item) {
   
      var isOverElementHeight = $.ui.isOverAxis(this.positionAbs.top + this.offset.click.top, item.top, item.height), isOverElementWidth = $.ui.isOverAxis(this.positionAbs.left + this.offset.click.left, item.left, item.width), isOverElement = isOverElementHeight && isOverElementWidth;
      
      /* cms-addition:
       *  verify if item parent matches the container the pointer intersect with */
      var isOverElementOld = isOverElement;
      
      if (isOverElement) {
         isOverElement = false;
         for (var i = this.containers.length - 1; i >= 0; i--) {
            if (this._intersectsWith(this.containers[i].containerCache)) {
               if (this.containers[i].element[0] == item.item.parent().get(0)) {
                  isOverElement = true;
                  break;
               }
            }
         }
      }
      
      if (!isOverElement) 
         return false;
      
      var verticalDirection = this._getDragVerticalDirection(), horizontalDirection = this._getDragHorizontalDirection();
      
      return this.floating ? (((horizontalDirection && horizontalDirection == "right") || verticalDirection == "down") ? 2 : 1) : (verticalDirection && (verticalDirection == "down" ? 2 : 1));
      
   },
   /**
    * Refreshes the list of sortable items.<p>
    *
    * @param {Object} event
    */
   _refreshItems: function(event) {
      /*
       * cms-addition:
       * the arrays this.containers and queries are filled in a different order to avoid problems with favorites-dropzone
       */
      this.items = [];
      this.containers = [];
      var items = this.items;
      var self = this;
      var queries = [];
      var connectWith = this._connectWith();
      
      if (connectWith) {
         for (var i = connectWith.length - 1; i >= 0; i--) {
            var cur = $(connectWith[i]);
            for (var j = cur.length - 1; j >= 0; j--) {
               var inst = $.data(cur[j], 'sortable');
               if (inst && !inst.options.disabled) {
                  queries.push([$.isFunction(inst.options.items) ? inst.options.items.call(inst.element[0], event, {
                     item: this.currentItem
                  }) : $(inst.options.items, inst.element), inst]);
                  this.containers.push(inst);
               }
            };
                     };
               } else {
         this.containers = [this];
         queries = [[$.isFunction(this.options.items) ? this.options.items.call(this.element[0], event, {
            item: this.currentItem
         }) : $(this.options.items, this.element), this]];
      }
      
      for (var i = queries.length - 1; i >= 0; i--) {
         var targetData = queries[i][1];
         var _queries = queries[i][0];
         
         for (var j = 0, queriesLength = _queries.length; j < queriesLength; j++) {
            var item = $(_queries[j]);
            
            item.data('sortable-item', targetData); // Data for target checking (mouse manager)
            items.push({
               item: item,
               instance: targetData,
               width: 0,
               height: 0,
               left: 0,
               top: 0
            });
         };
               };
      
         },
   /**
    * Internal event-handler for the mouse-move event. Introducing a workaround for a bug in IE8
    * that sets the event.button property to 0 even with the mouse button still pressed.<p>
    *
    * @param {Object} event the mouse-move event
    */
   _mouseMove: function(event) {
      // IE mouseup check - mouseup happened when mouse was out of window
      /*
       * cms-addition:
       * don't do this in IE8 as event.button may be 0 even with the mouse button down
       */
      if ($.browser.msie && $.browser.version < 8 && !event.button) {
         return this._mouseUp(event);
      }
      
      if (this._mouseStarted) {
         this._mouseDrag(event);
         return event.preventDefault();
      }
      
      if (this._mouseDistanceMet(event) && this._mouseDelayMet(event)) {
         this._mouseStarted = (this._mouseStart(this._mouseDownEvent, event) !== false);
         (this._mouseStarted ? this._mouseDrag(event) : this._mouseUp(event));
      }
      
      return !this._mouseStarted;
   }
})


/**
 * Helper function to deactivate a button in the toolbar
 *
 * @param {Object} elem the button DOM object to be deactivated
 */
var deactivateButton = function(elem, info) {
   var $elem = $(elem);
   var w = $elem.outerWidth();
   var h = $elem.outerHeight();
   var pos = $elem.offset();
   var toolbarOffset = $('#toolbar_content').offset();
   var left = pos.left - toolbarOffset.left;
   var top = pos.top - toolbarOffset.top;
   var $overlay = $('<div/>').css({
      'position': 'absolute',
      'opacity': '0.5',
      'background-color': '#888888',
      'width': w,
      'height': h,
      'top': top,
      'left': left,
      'z-index': '999999'
   });
   $overlay.attr('title', info);
   $overlay.appendTo('#toolbar_content');
}

/**
 * Application entry point.
 */
$('document').ready(function() {

   // TODO: may be it is better to load the toolbar after successfully loading the data
   $('.cms-item a.ui-icon').live('click', cms.toolbar.toggleAdditionalInfo);
   cms.toolbar.addToolbar();
   $('.cms-item-list div.cms-additional div').jHelperTip({
      trigger: 'hover',
      source: 'attribute',
      attrName: 'alt',
      topOff: -30,
      opacity: 0.8,
      live: true
   });
   cms.toolbar.dom.toolbar.css('cursor', 'wait');
   var M = cms.messages;
   if (cms.data.NO_EDIT_REASON) {
      //$('#toolbar_content button').addClass('cms-deactivated').unbind('click');
      // TODO: better display an red-square-icon in the toolbar with a tooltip
      cms.util.dialogAlert(cms.data.NO_EDIT_REASON, M.GUI_CANT_EDIT_TITLE_0);
      var buttonSelector = cms.util.makeCombinedSelector(['move', 'delete', 'add', 'new', 'reset', 'favorites', 'storage', 'properties'], '#toolbar_content button[name="%"]');
      var $buttons = $(buttonSelector);
      $buttons.unbind('click').unbind('mouseover').addClass('cms-deactivated').each(function() {
         deactivateButton(this, cms.data.NO_EDIT_REASON);
      });
   } else if (cms.util.isFirebugActive()) {
      cms.util.dialogAlert(M.GUI_FIREBUG_ACTIVE_0, M.GUI_FIREBUG_ACTIVE_TITLE_0)
   }
   
   cms.data.loadAllData(function(ok) {
      if (ok) {
         cms.data.fillContainers();
         cms.toolbar.toolbarReady = true;
         cms.toolbar.dom.toolbar.css('cursor', '');
         
         $(document).trigger("cms-data-loaded");
      } else {
            // TODO
      }
   });
});
